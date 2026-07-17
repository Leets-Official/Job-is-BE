# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Behavioral guidelines

Bias toward caution over speed; for trivial tasks, use judgment.

1. **Think before coding.** State assumptions explicitly. If multiple interpretations exist, present them instead of picking silently. If something is unclear, stop and ask.
2. **Simplicity first.** Minimum code that solves the problem — no speculative features, abstractions for single-use code, or error handling for impossible scenarios.
3. **Surgical changes.** Touch only what the task requires. Don't refactor or "improve" adjacent code. Match existing style. Remove imports/vars your change orphaned; leave pre-existing dead code (mention it instead).
4. **Goal-driven execution.** Turn tasks into verifiable goals (e.g. "fix the bug" → "write a failing test, then make it pass") so you can loop to a verified result instead of stopping at "looks right."

## Commands

Gradle wrapper (Windows: `gradlew.bat`, otherwise `./gradlew`):

- Build: `./gradlew build`
- Run locally: `./gradlew bootRun` (defaults to the `dev` profile — see Configuration below)
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.leets7th.job_is_be.JobIsBeApplicationTests"`
- Run a single test method: `./gradlew test --tests "com.leets7th.job_is_be.JobIsBeApplicationTests.methodName"`

There is no linter/formatter configured in this repo.

## Architecture

Package root: `com.leets7th.job_is_be`. The project is early-stage — most domains currently only have JPA entities/enums, no repositories, services, or controllers yet.

- `domain/{deck,job,notification,personality,user}/{entity,enums}` — domain-driven package layout, one package per bounded context.
- `global/` — cross-cutting concerns shared across domains:
  - `base` — `BaseEntity` (adds `createdAt`/`updatedAt` via JPA auditing) and the `BaseStatus` interface (`httpStatus`, `code`, `message`) that result-code enums implement.
  - `status` — `ErrorStatus` / `SuccessStatus` enums implement `BaseStatus`. Add new API result codes here rather than constructing ad-hoc messages.
  - `exception` — `GeneralException(BaseStatus)` is the standard exception to throw from application code. `GeneralExceptionAdvice` (`@RestControllerAdvice`) converts it — plus `IllegalArgumentException`, `NullPointerException`, and validation errors — into `ApiResponse` error bodies.
  - `response` — `ApiResponse<T>` is the single response envelope (`isSuccess`, `code`, `message`, `data`), built via `ApiResponse.success(...)`/`ApiResponse.error(...)`. `PageResponse<T>` wraps Spring `Page<T>` for paginated endpoints.
  - `config` — `SecurityConfig` (stateless, CSRF disabled; all requests currently `permitAll()` since auth isn't wired up yet), `WebConfig` (CORS from `app.cors.allowed-origins`), `SwaggerConfig` (OpenAPI at `/swagger-ui.html`, JWT bearer scheme pre-registered), `JpaConfig` (`@EnableJpaAuditing`, required for `BaseEntity` timestamps to populate).
- Entity conventions: extend `BaseEntity`, use `@NoArgsConstructor(access = AccessLevel.PROTECTED)` with a Lombok `@Builder` all-args constructor, and expose state transitions as instance methods (e.g. `Job.expire()`, `Job.markRemoved()`) instead of public setters.

### Configuration

- `application.yml` holds profile-agnostic settings; active profile defaults to `dev` via `SPRING_PROFILES_ACTIVE`.
- `application-dev.yml` is gitignored and must exist locally (or be supplied via env vars) for the `dev` profile to start — it carries the Postgres connection (`DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD`), `ddl-auto: update`, and `JWT_SECRET`.
- OAuth (Kakao/Google) and CORS settings in `application.yml` are also env-var driven (`KAKAO_*`, `GOOGLE_*`, `CORS_ALLOWED_ORIGINS`) with dev-safe defaults.
