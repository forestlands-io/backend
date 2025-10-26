# Forestlands – Functional Specification (MVP: Focus Timer Only)

_Last updated: 2025-10-24_

## 1) Overview

Forestlands is a gamified focus-timer. Each successful focus session (“pomodoro”) grows progress and rewards **soft currency** (internally `soft_currency`). Users can unlock tree species using soft or hard currency (internally `soft_currency` / `hard_currency`). **Map and inventory are explicitly out of scope for this MVP.**

MVP is **backend-first** with a single Spring Boot service and MySQL. iOS and Web are thin shells over the API.

---

## 2) Scope (MVP)

- **Focus Timer System**
    - Users start and end focus sessions.
    - Server validates timing, tolerates small drift, and records results.
    - Rewards **soft currency** on success.
- **Economy**
    - Two balances: `soft_currency` and `hard_currency`.
    - `soft_currency` is earned from successful sessions.
    - `hard_currency` exists in schema; not purchasable in MVP (admin/test grants only).
    - Unlockable species (non-premium unlock with `soft_currency`, premium unlock with `hard_currency`) — **permanent unlocks**.
- **Species**
    - 3–5 default species + 3–5 unlockable (seeded via migration).
    - Each session is tied to a chosen species (must be unlocked/available).

> **Out of scope in this MVP:** inventory, map, placement, biomes, buildings, social, enforcement, purchases.

---

## 3) Domain Rules

### 3.1 Focus Sessions
- Client selects:
    - **Duration:** 10–120 minutes (UI steps of 5; server enforces only min/max).
    - **Species:** must be unlocked/available to the user at start.
    - Optional **tag** (≤ 30 chars) for reporting.
- Each session has a **client UUID**; server also has numeric PK.
- Lifecycle:
    - `session_start` → server stamps `server_start_time`.
    - `session_end(state)` → server stamps `server_end_time` and validates.
- **State machine:** `CREATED → (SUCCESS | INTERRUPTED)`
- **Validation**
    - Min length: **5 min**
    - Max length: **120 min**
    - **Drift tolerance:** ± **3 min** between client- and server-stamped intervals.
    - Overlaps **allowed** (no single-active enforcement).
    - Server logs mismatches/anomalies but does not penalize.
- **Rewards**
    - On `SUCCESS`: award **soft currency = validated_minutes** (computed from server interval, clamped to [5,120]).
    - On `INTERRUPTED`: no reward.

### 3.2 Economy
- **Wallet** per user with integer balances: `soft_currency`, `hard_currency`.
- All wallet mutations occur **server-side** with **audit logging**:
    - Reason codes: `FOCUS_REWARD`, `SPECIES_UNLOCK_SOFT`, `SPECIES_UNLOCK_HARD`, `ADMIN_ADJUSTMENT`, `TEST_GRANT`, etc.
- **Species unlocks**
    - Non-premium: cost in **soft_currency**.
    - Premium: cost in **hard_currency**.
    - Unlock is **permanent** for the user.

---

## 4) System Design

### 4.1 Tech Stack
- **Backend:** Java 21, Spring Boot 3, MySQL 8, Hibernate, Flyway.
- **Service:** single monolith.
- **Time & IDs:** server times in UTC; client-provided UUIDs validated/stored.

### 4.2 Persistence Model (MVP)
- `user`
    - `id` (PK, bigint), `uuid` (char(36), unique)
    - `email` (unique), `password_hash`
    - `created_at`, `updated_at`
- `wallet`
    - `id` (PK), `user_id` (FK)
    - `soft_currency` (int), `hard_currency` (int)
    - `updated_at`
- `wallet_ledger`
    - `id` (PK), `user_id` (FK)
    - `delta_soft_currency` (int), `delta_hard_currency` (int)
    - `reason` (varchar), `ref_type` (varchar), `ref_id` (bigint/uuid)
    - `created_at`
- `species`
    - `id` (PK), `uuid` (char(36), unique), `code` (varchar, unique)
    - `name` (varchar), `is_premium` (bool)
    - `price` (int) — interpreted as **soft** if `is_premium=false` else **hard**
    - `is_enabled` (bool), `default_available` (bool)
    - `created_at`, `updated_at`
- `user_species_unlock`
    - `id` (PK), `user_id` (FK), `species_id` (FK)
    - `unlocked_at` (datetime), `method` enum(`SOFT_CURRENCY`,`HARD_CURRENCY`,`ADMIN_GRANT`,`TEST_GRANT`)
    - `price_paid` (int), `currency_type` enum(`SOFT`,`HARD`,`NONE`), `notes` (varchar, nullable)
    - Unique `(user_id, species_id)`; index on `user_id`, `species_id`
- `focus_session`
    - `id` (PK), `uuid` (char(36), unique), `user_id` (FK), `species_id` (FK → `species.id`)
    - `client_start_time`, `client_end_time`
    - `server_start_time`, `server_end_time`
    - `state` enum(`CREATED`,`SUCCESS`,`INTERRUPTED`)
    - `tag` (varchar(20), nullable)
    - `planned_minutes` (int, nullable)
    - `duration_minutes` (int) — validated server-side (clamped on success)
    - `flags_json` (json) — anomaly notes (e.g., drift alerts)
    - `created_at`, `updated_at`

---

## 5) API (MVP)

Base URL: `/api/v1`  
Auth: Bearer JWT (email/password login to obtain token).  
All timestamps are **ISO-8601 UTC**.

### 5.1 Auth
- `POST /auth/register`
    - body: `{ "email": "...", "password": "..." }`
    - 201 → `{ "userId": 123, "userUuid": "..." }`
- `POST /auth/login`
    - body: `{ "email": "...", "password": "..." }`
    - 200 → `{ "accessToken": "..." }`
- `POST /auth/request-password-reset`
- `POST /auth/reset-password`

### 5.2 Species
- `GET /species`
    - 200 → list of `{ "uuid", "name", "isPremium", "price", "isEnabled", "unlocked" }`
- `POST /species/{speciesUuid}/unlock`
    - Validates currency based on `isPremium`.
    - Deducts from wallet with ledger entry.
    - 200 → `{ "unlocked": true, "wallet": { "soft_currency": 123, "hard_currency": 0 } }`

### 5.3 Focus Sessions
- `POST /focus/sessions/start`
    - body:
      ```json
      {
        "sessionUuid": "...",
        "speciesCode": "red_cherry_2",
        "clientStartTime": "2025-10-24T10:00:00Z",
        "plannedMinutes": 25,
        "tag": "Deep Work"
      }
      ```
    - Rules:
        - `plannedMinutes` 10–120 (UI may step 5).
        - `speciesCode` optional; when present must match `[A-Za-z0-9_]+` and refer to an enabled species unlocked by the user (unless `default_available`).
        - `tag` optional; when present must match `[A-Za-z0-9 ]+` (max 20 chars).
        - Species must be unlocked or default-available.
    - 201 → `{ "id": 456, "serverStartTime": "..." }`
- `POST /focus/sessions/{sessionUuid}/end`
    - body: `{ "clientEndTime": "2025-10-24T10:25:00Z", "state": "SUCCESS" }`
    - Server stamps `serverEndTime`, validates duration & drift:
        - Min 5, max 120, drift ±3 min.
    - On `SUCCESS`:
        - Award `"soft_currency_awarded" = validatedMinutes`.
        - Ledger entry with `reason = "FOCUS_REWARD"`, `ref_type = "FOCUS_SESSION"`, `ref_id = focus_session.id`.
    - 200 →
      ```json
      {
        "state": "SUCCESS",
        "validatedMinutes": 25,
        "soft_currency_awarded": 25,
        "wallet": { "soft_currency": 123, "hard_currency": 0 },
        "anomalies": ["DRIFT_2M"]
      }
      ```
- `GET /focus/sessions?limit=...&cursor=...`
    - Paged list for history & reporting.

### 5.4 Wallet
- `GET /wallet`
    - 200 → `{ "soft_currency": 123, "hard_currency": 0 }`
- `GET /wallet/ledger?limit=...&cursor=...`
    - 200 → list of entries with reason, deltas, refs.

---

## 6) Validation & Edge Cases

- **Drift calculation:** compare (`clientEnd - clientStart`) vs (`serverEnd - serverStart`). Accept within ±3 minutes. Use the **server** interval for reward, clamped to [5, 120].
- **Overlapping sessions:** allowed; server does not reject.
- **Species disabled:** if `is_enabled = false`, cannot be chosen nor unlocked; users who unlocked earlier retain it.
- **Idempotency:** `sessionUuid` unique; duplicate start or end calls with same UUID are idempotent.
- **Auditability:** every wallet mutation writes a ledger record with reference to the originating operation.

---

## 7) Seed Data (Flyway)

- Default species (enabled): e.g., `Oak`, `Pine`, `Birch`.
- Unlockable species: e.g., `Maple`, `Cherry`, `Cypress` with `price` interpreted by `is_premium`.
- Admin/test tools to grant `hard_currency` for QA only.

---

## 8) Security & Compliance

- Passwords hashed (bcrypt/argon2id).
- JWT (short TTL).
- Rate limiting on auth endpoints.
- PII minimal: email only.
- Manual account deletion via admin SQL in MVP.

---

## 9) Observability

- Structured logs for:
    - Focus validation (durations, drift, flags).
    - Wallet mutations and failures.
- Basic metrics:
    - Sessions started / finished / success rate.
    - Avg validated minutes.
    - Soft currency minted/day.
    - Unlock conversions.

---

## 10) Acceptance Criteria (MVP)

- User can register, login, start/end focus sessions.
- Success → `soft_currency_awarded = validated minutes`; failure → no award.
- User can view wallet and species; unlock a species with sufficient balance.
- All wallet changes are ledgered; species unlocks are permanent.
