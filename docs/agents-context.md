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

## Implementation Notes
- **Time:** store UTC; use `Instant`.
- **IDs:** client `sessionUuid` is idempotency key; validate UUID.
- **Validation:** clamp to [5,120]; enforce drift tolerance of ±3 minutes (not persisted).
- **Input Sanitization:** `speciesCode` accepts only alphanumeric + underscore; `tag` limited to alphanumeric + spaces (≤20 chars).
- **Species Access:** enforce `enabled` flag and require unlock unless `defaultAvailable = true`.
- **Auth:** JWT (short TTL), Argon2 for passwords.
- **Ledger:** every wallet mutation must include `reason` and reference (type+id).
- **Migrations:** Flyway baseline + seeds for species; admin/test endpoints to grant `hard_currency`.
- **Logging:** structured; include `sessionUuid`, `userUuid` in events.
- **Testing:** repository slice tests, MVC tests, and endpoint contract tests.

## Definition of Done
- All endpoints above implemented with validation, persistence, and tests.
- Species unlocks & wallet ledger are consistent and idempotent.
- Successful session rewards `soft_currency` exactly equal to validated minutes.
- Flyway migrations apply cleanly against empty MySQL 8.
