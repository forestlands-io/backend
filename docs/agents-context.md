# Agents Context – Forestlands Backend (MVP: Focus Timer Only)

## Mission
Implement a clean, testable Spring Boot 3 + MySQL 8 backend that powers:
1) Focus sessions with server-side validation & rewards.
2) Simple economy with two balances: `soft_currency` and `hard_currency`.
3) Species listing & permanent unlocks.

> **Out of scope for now:** inventory and map (remove completely), purchases, enforcement, social.

## Service Coordinates
- Group: `io.forestlands`
- Artifact: `backend`
- Java: 21
- Package root: `io.forestlands.backend`

## Core Rules (must-implement)
- Duration bounds: **5–120** minutes; UI may step 5 but server enforces bounds only.
- Drift tolerance: **±3 minutes** between client and server intervals.
- Overlapping sessions: **allowed**.
- Rewards on `SUCCESS`: **`soft_currency` = validated minutes** (use server diff; clamp bounds).
- Species unlocks are **permanent**; price interpreted by `is_premium`:
    - if `false` → cost in `soft_currency`
    - if `true` → cost in `hard_currency`

## Entities
- `User(id, uuid, email, passwordHash, createdAt, updatedAt)`
- `Wallet(id, userId, soft_currency, hard_currency, updatedAt)`
- `WalletLedger(id, userId, delta_soft_currency, delta_hard_currency, reason, refType, refId, createdAt)`
- `Species(id, uuid, name, is_premium, price, is_enabled, createdAt, updatedAt)`
- `UserSpeciesUnlock(id, userId, speciesId, unlockedAt)` unique(userId,speciesId)
- `FocusSession(id, uuid, userId, speciesId, clientStart, clientEnd, serverStart, serverEnd, state, tag, durationMinutes, driftMinutes, flagsJson, createdAt, updatedAt)`

## API Sketch
- `POST /auth/register`, `POST /auth/login`, reset endpoints.
- `GET /species`, `POST /species/{uuid}/unlock`
- `POST /focus/sessions/start` → `{sessionUuid, speciesUuid, clientStartTime, plannedMinutes, tag?}`
- `POST /focus/sessions/{sessionUuid}/end` → `{clientEndTime, state}` → awards `soft_currency` on success.
- `GET /focus/sessions?limit&cursor`
- `GET /wallet`, `GET /wallet/ledger?limit&cursor`

## Implementation Notes
- **Time:** store UTC; use `Instant`.
- **IDs:** client `sessionUuid` is idempotency key; validate UUID.
- **Validation:** clamp to [5,120]; compute drift = |clientDur - serverDur|.
- **Auth:** JWT (short TTL), bcrypt/argon2id for passwords.
- **Ledger:** every wallet mutation must include `reason` and reference (type+id).
- **Migrations:** Flyway baseline + seeds for species; admin/test endpoints to grant `hard_currency`.
- **Logging:** structured; include `sessionUuid`, `userId` in events.
- **Testing:** repository slice tests, MVC tests, and endpoint contract tests.

## Definition of Done
- All endpoints above implemented with validation, persistence, and tests.
- Species unlocks & wallet ledger are consistent and idempotent.
- Successful session rewards `soft_currency` exactly equal to validated minutes.
- Flyway migrations apply cleanly against empty MySQL 8.
