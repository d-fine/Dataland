# Research: Deactivate Non-Sourceability via Endpoint

**Feature**: `010-nonsource-deactivate`  
**Phase**: 0 — Research

No NEEDS CLARIFICATION items were identified in the Technical Context. This document records
the discoveries made by reading existing source code that inform the design decisions in Phase 1.

---

## 1. Existing Request Model

**File**: `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/metainformation/NonSourceabilityRequest.kt`

**Current fields**: `companyId` (required), `dataType` (required), `reportingPeriod` (required), `reason` (required), `bypassQa` (optional, defaults `false` via `@field:JsonProperty(required = false)`).

**Pattern for required fields**: `@field:JsonProperty(required = true)` with no Kotlin default.

**Decision**: `currentlyActive: Boolean` is added as a **required** field (`@field:JsonProperty(required = true)`) — consistent with how other mandatory fields in this class are declared. Callers that omit the field receive HTTP 400 from the JSON deserializer.

---

## 2. Existing Service Logic

**File**: `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/NonSourceabilityInformationManager.kt`

**Current constraint check** (in `processNonSourceabilityRequest`):
```
blockedStatuses = [Pending, Accepted]
existsActiveOrPendingForTuple(…, blockedStatuses) → reject with InvalidInputApiException
```

**Current value derivation**:
```
qaStatus      = if (bypassQa) Accepted else Pending
currentlyActive = bypassQa   // always mirrors bypassQa today
```

**Existing utilities already available**:
- `findActiveForTuple(companyId, dataType, reportingPeriod): List<…>` — returns the currently-active entry (if any). Used by `isCurrentlyActive` and `deactivateForTriple`.
- `existsActiveOrPendingForTuple(…, blockedStatuses: List<QaStatus>): Boolean` — generic check. Calling it with `listOf(QaStatus.Pending)` gives the pending-only check needed by the new logic.
- `deactivateForTriple(…)` — bulk-sets `currentlyActive=false` on all active entries (used by the dataset-upload deactivation path). **Not reused for the reversal flow** because the reversal must also create a new entry, and should not emit a lifecycle event.

**Decision**: No new repository methods required. The refactored service calls the two existing repository methods with different argument combinations per case.

---

## 3. Lifecycle Event Rules

**File**: `NonSourceabilityInformationManager.emitLifecycleEvent`

Current logic always emits one of:
- `NON_SOURCEABILITY_CREATED` (bypassQa=false, QA path)
- `NON_SOURCEABILITY_AUTO_ACCEPTED` (bypassQa=true, admin bypass)

**FR-010 ruling**: The `bypassQa=true, currentlyActive=false` (reversal) path MUST NOT emit any event. The data-sourcing service must not be triggered.

**Decision**: `emitLifecycleEvent` is only called when `currentlyActive=true` OR `bypassQa=false`. The private helper remains unchanged; the call-site is guarded by the new branching logic.

---

## 4. Exception Types

**File**: `dataland-backend-utils/src/main/kotlin/org/dataland/datalandbackendutils/exceptions/`

| Exception class | HTTP status | Use |
|---|---|---|
| `InvalidInputApiException` | 400 | Invalid combination (`bypassQa=false, currentlyActive=true`) |
| `ConflictApiException` | 409 | All constraint-violation rejections (active exists, pending exists, already sourceable) |

`ConflictApiException` already exists and maps to `HttpStatus.CONFLICT` (409). This matches the spec requirement (FR-003–FR-006, FR-008, FR-009 → 409). The existing duplicate check used `InvalidInputApiException` (400); this will be changed to `ConflictApiException` for the constraint cases to align with the spec.

---

## 5. Database Schema Impact

**`currently_active` column**: Already present in `non_sourceability_information` table (created in `V13__CreateNonSourceabilityInformation`). No migration required.  
**`NonSourceabilityRequest` is a DTO only** — not persisted. No schema change at all.

---

## 6. Call-site Impact

Adding `currentlyActive` as a **required** field to `NonSourceabilityRequest` (the source Kotlin model) breaks all callers that construct the object:

| File | Type | Impact |
|---|---|---|
| `NonSourceabilityInformationManagerTest.kt` | backend unit/integration test | Update `request()` helper + all `NonSourceabilityRequest(...)` literals |
| `MetaDataControllerNonSourceableTest.kt` | backend controller test | Update all `NonSourceabilityRequest(...)` literals |
| `NonSourceabilityTest.kt` (e2e) | uses generated OpenAPI client model | Regenerate clients; update constructors after |
| `DataRequestNonSourceableTest.kt` (e2e) | uses generated OpenAPI client model | Regenerate clients; update constructors after |

The community-manager (`DataRequestNonSourceabilityManager`) reads `NonSourceabilityInformationResponse` only — it does NOT construct `NonSourceabilityRequest`. No changes required there.

---

## 7. Test Infrastructure Pattern

The preferred pattern for new service-level integration tests in `dataland-backend` is `BaseIntegrationTest` (from `dataland-backend-utils`), which provides:
- Real Postgres container via `TestPostgresContainer`
- `@Transactional @Rollback` — data reset between tests automatically
- `@SpringBootTest` with `properties = ["spring.profiles.active=containerized-db"]`

The existing `NonSourceabilityInformationManagerTest` was written before this pattern and uses H2 + `@DirtiesContext`. The new tests should migrate this class to extend `BaseIntegrationTest` and drop the H2/`@DirtiesContext`/`@AutoConfigureTestDatabase` annotations.

**Decision**: Migrate `NonSourceabilityInformationManagerTest` to `BaseIntegrationTest` while adding the new test cases.

---

## 8. Tuple vs Triple Naming

The repository methods use the word "Tuple" (e.g. `existsActiveOrPendingForTuple`, `findActiveForTuple`) while the domain language (spec, PR descriptions, business discussions) uses "triple". This pre-existing inconsistency is not corrected in this feature's scope to keep the change minimal. References in this plan use "triple" for domain language and "Tuple" only when referring to the actual method names.

---

## Summary of Decisions

| Decision | Chosen | Rationale |
|---|---|---|
| `currentlyActive` required vs optional | Required | Explicit intent, existing pattern for mandatory fields |
| Constraint violation HTTP code | 409 via `ConflictApiException` | Matches spec; existing class already in codebase |
| Invalid combination HTTP code | 400 via `InvalidInputApiException` | Input validation, not a state conflict |
| New repository methods | None | Existing methods cover all new query patterns |
| DB migration | None | `currently_active` column already exists |
| Lifecycle event on reversal | None emitted | FR-010; data-sourcing must not be triggered |
