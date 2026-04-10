# Tasks: NonSourceability QA Lifecycle E2E Test

**Input**: Design documents from `/specs/005-nonsourceability-e2e-tests/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the required imports to `NonSourceabilityTest.kt` so the new test method compiles.

No new files, no new modules. The only setup is extending the existing import block in one file.

- [x] T001 Add missing imports to dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt: `awaitUntilAsserted`, `NonSourceabilityRequest`, `NonSourceabilityInformationResponse`, `DataTypeEnum`, backend `QaStatus`, QA-service `QaStatus` (aliased), `NonSourceableQaReviewInformation`

---

## Phase 2: Foundational (Blocking Prerequisites)

No foundational infrastructure tasks — all required API clients (`metaDataControllerApi`, `nonSourceabilityQaControllerApi`), auth helpers (`GlobalAuth`, `TechnicalUser`), and async utilities (`awaitUntilAsserted`) already exist in the project.

**Checkpoint**: Phase 1 complete → ready to implement the test method.

---

## Phase 3: User Story 1 - Full QA Lifecycle for Non-Sourceability (Priority: P1) 🎯 MVP

**Goal**: Implement a single `@Test` method that exercises the full non-sourceability QA lifecycle end-to-end against a running Dataland stack.

**Independent Test**: Run `./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.POST nonSourceable bypassQa false triggers full QA lifecycle*"` (or wildcard match on the test name). Passes when all six state assertions pass.

### Implementation for User Story 1

- [x] T002 [US1] Upload a fresh company inside `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)` using `apiAccessor.uploadOneCompanyWithRandomIdentifier()` and capture `companyId` in dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt

- [x] T003 [US1] POST non-sourceability with `bypassQa=false` and `dataType=DataTypeEnum.sfdr` inside `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)`, capture the `NonSourceabilityInformationResponse`, and assert `qaStatus==Pending` and `currentlyActive==false` in dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt

- [x] T004 [US1] GET `metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(companyId, DataTypeEnum.sfdr, testReportingPeriod)` inside `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)` and assert: size==1, `companyId` matches, `dataType==sfdr`, `qaStatus==Pending`, `currentlyActive==false` in dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt

- [x] T005 [US1] Inside `awaitUntilAsserted`, call `nonSourceabilityQaControllerApi.getNonSourceableReviews(companyId=companyId, dataType=DataTypeEnum.sfdr.value, reportingPeriod=testReportingPeriod)` inside `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)` and assert: a matching row exists with `qaStatus==Pending` in dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt

- [x] T006 [US1] POST QA decision `Accepted` via `nonSourceabilityQaControllerApi.postNonSourceabilityDecision(nonSourceabilityId, QaStatus.Accepted)` inside `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)`, assert returned `NonSourceableQaReviewInformation.qaStatus==Accepted` in dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt

- [x] T007 [US1] Inside `awaitUntilAsserted`, re-fetch `metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(...)` inside `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)` and assert: `qaStatus==Accepted` and `currentlyActive==true` in dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt

**Checkpoint**: The single test method is complete and all six state assertions pass against a live stack.

---

## Phase 4: Polish & Cross-Cutting Concerns

- [x] T008 [P] Remove the now-redundant commented-out test stubs (the `/* @Test ... */` block at the bottom of the class) from dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt if they overlap with the new test; keep any that cover distinct scenarios

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: N/A — skipped, all infrastructure exists
- **User Story 1 (Phase 3)**: Depends on T001 (imports) being added first; T002–T007 are sequential steps within a single test method
- **Polish (Phase 4)**: Depends on Phase 3 completion

### User Story Dependencies

- **User Story 1 (P1)**: Only story — no cross-story dependencies

### Within User Story 1

Tasks T002–T007 form a strictly sequential narrative inside one `@Test` method:

```
T002: create company
  → T003: POST nonSourceable (bypassQa=false), assert Pending+inactive
    → T004: GET backend, assert Pending+inactive
      → T005: awaitUntilAsserted GET QA service, assert Pending row exists
        → T006: POST QA accept, assert returned row is Accepted
          → T007: awaitUntilAsserted GET backend, assert Accepted+active
```

### Parallel Opportunities

No parallel opportunities within this feature — all tasks touch the same single method in the same file, and steps T002–T007 are logically sequential.

---

## Parallel Example: User Story 1

```bash
# No parallelism — single test method, sequential steps.
# Run the test with:
./gradlew dataland-e2etests:test \
  --tests "org.dataland.e2etests.tests.NonSourceabilityTest.*"
```

---

## Implementation Strategy

**MVP scope**: The entire feature IS the MVP — one test method, one file.

**Suggested execution order**:
1. T001 — add imports (compiles the file)
2. T002–T007 — implement the test body step by step (each step is independently runnable once the previous assertion compiles)
3. T008 — optional cleanup of stale comments

**Key implementation notes** (from research.md):
- Use `org.dataland.datalandqaservice.openApiClient.model.QaStatus` for the `postNonSourceabilityDecision` call; alias it (e.g. `QaServiceQaStatus`) to avoid clash with `org.dataland.datalandbackend.openApiClient.model.QaStatus` used in backend GET assertions.
- `getNonSourceableReviews` takes `dataType: String?` — pass `DataTypeEnum.sfdr.value` (= `"sfdr"`), not the enum directly.
- `nonSourceabilityId` comes from the POST response body (`createdEntry.nonSourceabilityId`) — no secondary lookup needed.
- Wrap all API calls in `GlobalAuth.withTechnicalUser(TechnicalUser.Admin) { ... }`.
- Use `awaitUntilAsserted` only for the two async checkpoints (QA row appearance after backend POST, and backend `currentlyActive` update after QA acceptance).
