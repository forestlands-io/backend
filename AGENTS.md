# AGENTS.md — Working Instructions for AI Coding Agents

This document tells AI assistants how to operate inside the **Forestlands Backend** repository.

## Mission

Implement and evolve the **Forestlands** backend according to the functional spec while keeping the codebase simple, testable, and aligned with the MVP roadmap.

**Primary sources of truth (read first):**
- `/docs/functional-spec.md` - user-facing requirements (MVP vs post-MVP).
- `/docs/agents-context.md` - condensed, always-current summary for agents (entities, rules).
- `/docs/openapi.json` - current API spec, will be shared with frontend
- `/README.md` - setup and dev conventions.

> If these documents disagree, prefer `/docs/functional-spec.md`. Propose a fix via PR that updates both files consistently.

---

## Architectural Principles

- **Monolith** (Spring Boot 3, Java 21).
- **MySQL 8** with **Hibernate/JPA** for persistence.
- **Flyway** migrations (append-only). No destructive changes without explicit human approval.
- **REST + JWT** auth. Access tokens (≈15 min), refresh (≈30 days), issuer `forestlands`.
- **Deterministic behavior** and **observability** (logs and audit tables).

---

## Coding Conventions

- **Package root**: `io.forestlands.backend`.
- **Spring configuration**: Java config, no field injection; prefer constructor injection.
- **Controllers**: annotated with `@RestController`, return DTOs (no JPA entities).
- **DTO mapping**: write simple mappers.
- **Validation**: Bean Validation (`jakarta.validation`) on request DTOs; fail fast with meaningful messages.
- **Transactions**: service layer boundaries; never in controllers.
- **Errors**: centralized `@ControllerAdvice`; payload: `{ timestamp, path, code, message, details? }`.
- **Security**: Spring Security with JWT filter; Argon2 password encoder.
- **Testing**: JUnit 5; Repository tests with Testcontainers (MySQL); Service tests use mocks; Controller tests with `@WebMvcTest`.

---

## Migrations & Seed

- Create migrations under `src/main/resources/db/migration/` as `V###__description.sql`.
- Never edit old migration files unless explicitly asked. Add a new version.

---

## Logging & Audit

- Always add logger into services (private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);)
- Log all important events (e.g. user login, session start, species purchase).
- Use a **WalletLedger** table to record every coins/crystals change.

---

## What You May Implement Autonomously

- JPA entities + repositories per the domain above.
- Services with transactional boundaries.
- Controllers for MVP endpoints (auth, sessions, species, wallet, inventory, map).
- DTOs, mappers, validation annotations.
- Flyway migrations and seed data for species/map templates.
- Tests (unit + slice + Testcontainers).
- Documentation updates in `/README.md`, `/docs/functional-spec.md`, `/docs/agents-context.md`.

> Before introducing a new dependency, prefer standard Spring Boot starters. If a new dependency is essential, justify it in the PR description.

---

## Branching & Commits

- Branches: `<ticket_id>`, `fix_<short-name>`.
- Commits: conventional style:
    - `<ticket_id> - add inventory tree placement service`
    - `fix: prevent negative wallet deltas`
    - `chore: upgrade dependencies`

Open a PR with:
1. Summary of changes
2. Any new config/env keys
3. Migration numbers added
4. Sample curl if relevant

---

## Tooling Notes

- Assume **Maven** build (`mvn verify`, `mvn spring-boot:run`).
- Use **Testcontainers** for integration tests when DB is needed.
- Prefer Java time (`Instant`, `Duration`) and UTC everywhere.

---

## Security & Secrets

- No secrets in the repo. Use env.
- JWT secret must be provided for any non-test profile.
- Never log tokens, passwords, or PII.

---

## When In Doubt

- Re-read `/docs/functional-spec.md`.
- Prefer the simplest implementation that satisfies MVP.
- Add TODOs with a clear follow-up note and reference to the spec section.
- Propose small PRs; avoid “big bang” changes.