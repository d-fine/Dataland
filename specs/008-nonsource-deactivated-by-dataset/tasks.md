# Tasks: Non-Sourceability Deactivated by Dataset Upload

**Input**: Design documents from `/specs/008-nonsource-deactivated-by-dataset/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to
- Exact file paths included in all descriptions

---

## Phase 1: Setup

No project setup needed — this is a single-file addition to an existing test class. No new dependencies, no scaffolding.

---

## Phase 2: Foundational

No foundational prerequisites — all helper methods, auth utilities, and API clients are already present in `NonSourceabilityTest.kt`.

---

## Phase 3: User Story 1 - Non-Sourceability Deactivated by QA-Approved Dataset (Priority: P1) 🎯 MVP

**Goal**: Prove that once a QA-accepted dataset exists for a (companyId, dataType, reportingPeriod) triple, the corresponding non-sourceability entry's `currentlyActive` flag is set to `false`.

**Independent Test**: Run `./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.currentlyActive becomes false after QA approves a dataset for the same triple"` against a running stack.

### Implementation for User Story 1

- [x] T001 [P] [US1] Add private helper `uploadDatasetForTriple(ctx: Ctx): String` to `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt` — wraps `apiAccessor.uploadDummyFrameworkDataset(companyId, dataType, reportingPeriod, bypassQa=false)` and returns `dataId`
- [x] T002 [P] [US1] Add private helper `assertNonSourceabilityIsInactive(ctx: Ctx)` to `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt` — polls `metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets` inside `awaitUntilAsserted` until `currentlyActive == false`
- [x] T003 [US1] Add `@Test` method `` `currentlyActive becomes false after QA approves a dataset for the same triple` `` to `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt` — calls `postNonSourceableWithBypassQa`, `assertBackendEntryIsAcceptedAndActive`, `uploadDatasetForTriple`, `qaServiceControllerApi.changeQaStatus(Accepted)`, then `assertNonSourceabilityIsInactive` (depends on T001, T002)

**Checkpoint**: User Story 1 is complete and independently testable.

---

## Phase 4: Polish & Cross-Cutting Concerns

- [x] T004 [P] Verify no ktlint violations introduced by running `./gradlew dataland-e2etests:ktlintCheck` (or `ktlintFormat`) against `NonSourceabilityTest.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 3** (US1): No phase prerequisites — can start immediately.
- **Phase 4** (Polish): Depends on T003 being complete.

### User Story Dependencies

- **US1**: Only dependency is itself. T001 and T002 are independent of each other and can be written in parallel. T003 depends on both T001 and T002.

### Parallel Opportunities

- T001 and T002 can be written in any order or simultaneously (they touch different method definitions in the same file).
- T004 can run as soon as any of T001–T003 is written to catch formatting issues early.

---

## Parallel Example: User Story 1

```bash
# T001 and T002 are independent — write both helpers first, then wire the test
# T001: add uploadDatasetForTriple helper
# T002: add assertNonSourceabilityIsInactive helper
# T003: add the @Test method referencing both helpers
# T004: ktlint check
```

---

## Implementation Strategy

**MVP scope = entire feature** (only one user story). Start with T001 + T002 in parallel, then T003.

No new files. No new imports. Change is entirely additive to `NonSourceabilityTest.kt`.
