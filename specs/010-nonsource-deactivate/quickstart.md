# Quickstart: Deactivate Non-Sourceability via Endpoint

**Feature**: `010-nonsource-deactivate`  
**Phase**: 1 — Design

---

## What is being changed

One endpoint, one model, one service. No new files for the production code.

| File | Change |
|---|---|
| `NonSourceabilityRequest.kt` | Add required `currentlyActive: Boolean` field |
| `NonSourceabilityInformationManager.kt` | Refactor `processNonSourceabilityRequest` into 4 cases |
| `MetaDataApi.kt` | Update OpenAPI description + add 409 response code |
| `NonSourceabilityInformationManagerTest.kt` | Update existing tests; add ~8 new tests |
| `MetaDataControllerNonSourceableTest.kt` | Update existing call sites; add new tests |

---

## Step-by-step implementation order

### Step 1 — Update `NonSourceabilityRequest.kt`

Add `currentlyActive: Boolean` as the last field, marked required.  
Convention to follow (same file): `@field:JsonProperty(required = true)` + `@field:Schema(description = "…")`, no default value.

**After this step**: All existing callers of `NonSourceabilityRequest(…)` fail to compile. Proceed to Step 2 before running tests.

### Step 2 — Fix existing test call sites

In both `NonSourceabilityInformationManagerTest.kt` and `MetaDataControllerNonSourceableTest.kt`:
- Update the `request(bypassQa)` helper to accept a `currentlyActive: Boolean` parameter and pass it through.
- Update every inline `NonSourceabilityRequest(…)` literal to include `currentlyActive = <appropriate value>`.
  - Where `bypassQa=false` → `currentlyActive = false`
  - Where `bypassQa=true` → `currentlyActive = true`

**After this step**: Project compiles and all existing tests pass against the old service logic.

### Step 3 — Refactor `NonSourceabilityInformationManager.processNonSourceabilityRequest`

Replace the existing single-constraint check with four-way branching. Key rules:
- Import `ConflictApiException` from `dataland-backend-utils`.
- Check pending with `existsActiveOrPendingForTuple(…, listOf(QaStatus.Pending))`.
- Check active with `findActiveForTuple(…).isNotEmpty()`.
- For the reversal path: call `findActiveForTuple` once, save the result, use it for both the guard check and the entity to update.
- Call `emitLifecycleEvent` only when `currentlyActive=true` OR `bypassQa=false`.

> **Note on naming**: the repository methods use the word "Tuple" (e.g. `findActiveForTuple`) while the domain language uses "triple". This pre-existing inconsistency is **not** fixed in this feature's scope.

### Step 4 — Update `MetaDataApi.kt`

Add `currentlyActive` to the endpoint description. Add `ApiResponse(responseCode = "409", description = "Conflict — duplicate or invalid state.")` to the `@ApiResponses` annotation.

### Step 5 — Add new tests to `NonSourceabilityInformationManagerTest.kt`

Migrate and extend this test class to use `BaseIntegrationTest` (from `dataland-backend-utils`):  
`class NonSourceabilityInformationManagerTest : BaseIntegrationTest()`

This replaces the current H2 + `@AutoConfigureTestDatabase` + `@DirtiesContext` setup with a real Postgres container backed by `@Transactional @Rollback`. Profile annotation becomes `properties = ["spring.profiles.active=containerized-db"]`.

New test cases (service-level):

| Test name | Setup | Expected |
|---|---|---|
| `invalid combination bypassQa false currentlyActive true throws` | — | `InvalidInputApiException` |
| `bypassQa false currentlyActive false rejected when active entry exists` | Admin creates active entry first | `ConflictApiException` |
| `bypassQa false currentlyActive false rejected when pending entry exists` | Create pending entry first | `ConflictApiException` |
| `bypassQa true currentlyActive true rejected when active entry exists` | Admin creates active entry first | `ConflictApiException` |
| `bypassQa true currentlyActive true rejected when pending entry exists` | Create pending entry first | `ConflictApiException` |
| `bypassQa true currentlyActive false deactivates active entry` | Admin creates active entry first | Old entry `currentlyActive=false`, new entry returned, `isCurrentlyActive=false` |
| `bypassQa true currentlyActive false returns 409 when no active entry` | No entries | `ConflictApiException` |
| `bypassQa true currentlyActive false rejected when pending entry exists` | Create pending entry first | `ConflictApiException` |
| `bypassQa true currentlyActive false does not emit lifecycle event` | Admin creates active entry first | `cloudEventMessageHandler` called only once (for setup), zero times after reversal |

### Step 6 — Update `MetaDataControllerNonSourceableTest.kt`

Add controller-level tests for:
- Reversal succeeds for admin and returns OK.
- `isDataNonSourceable` returns 404 after successful reversal.

---

## Running the tests

```bash
# Service-level tests (uses Postgres test container — requires Docker):
./gradlew dataland-backend:test --tests "org.dataland.datalandbackend.services.NonSourceabilityInformationManagerTest"

# Full backend test suite:
./gradlew dataland-backend:test

# Lint:
./gradlew ktlintFormat
```

---

## E2E tests (out of scope — note for implementer)

After regenerating the OpenAPI client (`./gradlew dataland-e2etests:generateClients`), the following e2e test files will fail to compile until their `NonSourceabilityRequest(…)` constructors are updated with `currentlyActive`:
- `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt`
- `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/communityManager/DataRequestNonSourceableTest.kt`

These are tracked as a follow-up concern; the primary implementation scope is `dataland-backend`.
