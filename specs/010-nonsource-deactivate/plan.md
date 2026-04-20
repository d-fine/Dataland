# Implementation Plan: Deactivate Non-Sourceability via Endpoint

**Branch**: `010-nonsource-deactivate` | **Date**: 2026-04-17 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-nonsource-deactivate/spec.md`

## Summary

Extend the existing `POST /metadata/nonSourceable` endpoint by adding a required `currentlyActive` boolean field to `NonSourceabilityRequest`. This unlocks a fourth logical path (`bypassQa=true, currentlyActive=false`) that allows an admin to reverse a non-sourceability marking: the existing active entry is set to inactive, a new audit entry is created, no lifecycle message is emitted, and the triple is immediately treated as sourceable again. The other three combinations align constraint-checking to the new field semantics (active-entry check instead of accepted-status check), and one new invalid combination (`bypassQa=false, currentlyActive=true`) is explicitly rejected.

## Technical Context

**Language/Version**: Kotlin on JVM 21  
**Primary Dependencies**: Spring Boot, Spring Security, JPA/Hibernate, JUnit 5, H2 (test), `dataland-backend-utils` (exceptions), auto-generated OpenAPI clients  
**Storage**: PostgreSQL (`non_sourceability_information` table — no schema changes needed)  
**Testing**: `./gradlew dataland-backend:test` — Spring Boot integration tests using `BaseIntegrationTest` (real Postgres container, `containerized-db` profile, `@Transactional @Rollback`)  
**Target Platform**: Running Dataland backend service (Spring Boot)  
**Project Type**: Web service (REST API change within existing module)  
**Performance Goals**: N/A (single-row read/write per request)  
**Constraints**: No new dependencies; no DB migration; existing tests must continue to pass  
**Scale/Scope**: 5 files modified, ~8 new test cases, 0 new production files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Contract-First Service Boundaries | ✅ PASS | `NonSourceabilityRequest` is the contract; `currentlyActive` is added as a required field. OpenAPI spec regenerates automatically. Existing consumers (community-manager) read the response only — not affected. |
| II. Backward-Compatible Messaging | ✅ PASS | No new message types. The reversal path explicitly emits NO message (FR-010). |
| III. Microservice Autonomy | ✅ PASS | All changes are within `dataland-backend`. Tests use embedded H2 and mock `CloudEventMessageHandler`. |
| IV. Mandatory Test Coverage and Coverage Floor | ✅ PASS | All four new logic branches are covered by unit/integration tests in `NonSourceabilityInformationManagerTest`. Controller-level tests added for the reversal path. |
| V. Traceability, Validation, and Operational Clarity | ✅ PASS | Invalid combination rejected fast (400). State conflicts rejected as 409. Existing logger statements retained. |
| VI. Minimal Dependencies and Reviewable Changes | ✅ PASS | Zero new dependencies. Uses `ConflictApiException` already in `dataland-backend-utils`. 5 files touched. |

**Post-design re-check**: All gates pass. No violations.

## Project Structure

### Documentation (this feature)

```text
specs/010-nonsource-deactivate/
├── plan.md        # This file
├── research.md    # Phase 0 output
├── data-model.md  # Phase 1 output
├── quickstart.md  # Phase 1 output
└── tasks.md       # Phase 2 output (not yet created)
```

### Source Code (repository root)

```text
dataland-backend/src/main/kotlin/org/dataland/datalandbackend/
├── model/metainformation/
│   └── NonSourceabilityRequest.kt       # Add currentlyActive: Boolean (required)
├── services/
│   └── NonSourceabilityInformationManager.kt  # Refactor processNonSourceabilityRequest
└── api/
    └── MetaDataApi.kt                   # Update description + add 409 ApiResponse

dataland-backend/src/test/kotlin/org/dataland/datalandbackend/
├── services/
│   └── NonSourceabilityInformationManagerTest.kt  # Update helpers + ~8 new tests
└── controller/
    └── MetaDataControllerNonSourceableTest.kt     # Update call sites + new tests
```

**Note**: E2e tests (`dataland-e2etests`) use the generated OpenAPI client model and will need their `NonSourceabilityRequest(…)` constructors updated after `./gradlew dataland-e2etests:generateClients`. This is tracked as a follow-up scope item in [quickstart.md](quickstart.md).

## Complexity Tracking

No constitution violations. No entry required.

## Phase Plan

### Phase 0: Research

No unknowns. All relevant source files were inspected. Key findings:
- `ConflictApiException` (409) already exists in `dataland-backend-utils` — no new exception class needed.
- No DB migration required (`currently_active` column already exists).
- No new repository methods required (existing `findActiveForTuple` + `existsActiveOrPendingForTuple` cover all needed queries).
- Community-manager is not a caller of `postNonSourceabilityOfADataset` — not affected.

See [research.md](research.md) for full details.

### Phase 1: Design

See [data-model.md](data-model.md) for entity model and service branching logic, and [quickstart.md](quickstart.md) for step-by-step implementation order.

**Key decisions**:
- `currentlyActive` is **required** (not optional) — follows `@field:JsonProperty(required = true)` pattern.
- Constraint violations throw `ConflictApiException` → HTTP 409.
- Invalid combination (`bypassQa=false, currentlyActive=true`) throws `InvalidInputApiException` → HTTP 400.
- Reversal path (`bypassQa=true, currentlyActive=false`): deactivates active entry in place, creates audit entry, emits **no** lifecycle event.
- All logic changes are confined to `NonSourceabilityInformationManager.processNonSourceabilityRequest`; no other service is modified.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
