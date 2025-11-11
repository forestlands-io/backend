# Forestlands – Functional Specification (MVP: Focus Timer Only)

_Last updated: 2025-10-27_

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
- **Tree Inventory (backend tracking only)**
    - Every successful session now mints a tree of the chosen species into the user's inventory.
    - Inventory entries record placement status plus optional area-map coordinates for future features.
    - Users can fetch their current trees via `GET /api/v1/inventory/trees`.

> **Out of scope in this MVP:** map UI, placement UX, biomes, buildings, social, enforcement, purchases. Inventory is tracked server-side only.

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

### 3.3 Tree Inventory
- Schema: `user_id`, `species_id`, `is_placed`, `cell_x`, `cell_y` (nullable) plus audit columns.
- When a focus session ends in `SUCCESS`, award **one** tree for the chosen species and mark it as unplaced (`is_placed = false`).
- Trees can later be placed onto the area map; once `is_placed = true` the record becomes immutable (no moving/removing in MVP).
- Placement coordinates (`cell_x`, `cell_y`) remain `NULL` until the user places the tree.

---

## 4) System Design

### 4.1 Tech Stack
- **Backend:** Java 21, Spring Boot 3, MySQL 8, Hibernate, Flyway.
- **Service:** single monolith.
- **Time & IDs:** server times in UTC; client-provided UUIDs validated/stored.

### 4.2 API (MVP)

Base URL: `/api/v1`  
Auth: Bearer JWT (email/password login to obtain token).  
All timestamps are **ISO-8601 UTC**.


---

## 5) Validation & Edge Cases

- **Drift calculation:** compare (`clientEnd - clientStart`) vs (`serverEnd - serverStart`). Accept within ±3 minutes. Use the **server** interval for reward, clamped to [5, 120].
- **Overlapping sessions:** allowed; server does not reject.
- **Species disabled:** if `is_enabled = false`, cannot be chosen nor unlocked; users who unlocked earlier retain it.
- **Idempotency:** `sessionUuid` unique; duplicate start or end calls with same UUID are idempotent.
- **Auditability:** every wallet mutation writes a ledger record with reference to the originating operation.

---

## 6) Seed Data (inserted manually)

- Default species (enabled): e.g., `Oak`, `Pine`, `Birch`.
- Unlockable species: e.g., `Maple`, `Cherry`, `Cypress` with `price` interpreted by `is_premium`.
- Admin/test tools to grant `hard_currency` for QA only.

---

## 7) Security & Compliance

- Passwords hashed (Argon2).
- JWT (short TTL).
- Rate limiting on auth endpoints.
- PII minimal: email only.
- Manual account deletion via admin SQL in MVP.

---

## 8) Observability

- Structured logs for:
    - Focus validation (durations, drift, flags).
    - Wallet mutations and failures.
- Basic metrics:
    - Sessions started / finished / success rate.
    - Avg validated minutes.
    - Soft currency minted/day.
    - Unlock conversions.

---

## 9) Acceptance Criteria (MVP)

- User can register, login, start/end focus sessions.
- Success → `soft_currency_awarded = validated minutes`; failure → no award.
- User can view wallet and species; unlock a species with sufficient balance.
- All wallet changes are ledgered; species unlocks are permanent.
