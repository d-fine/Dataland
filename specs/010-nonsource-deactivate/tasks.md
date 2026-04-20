# Tasks: Deactivate Non-Sourceability via Endpoint

**Input**: Design documents from `/specs/010-nonsource-deactivate/`
**Branch**: `010-nonsource-deactivate`
**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | quickstart.md ✅

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Compile-safe foundation — add the new required field so all dependent changes build correctly.

- [X] T001 Add required `currentlyActive: Boolean` field to `NonSourceabilityRequest` in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/metainformation/NonSourceabilityRequest.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fix all existing call sites that construct `NonSourceabilityRequest` so the project compiles and existing tests pass. Must be complete before any logic or test changes.

**⚠️ CRITICAL**: Nothing else can be built or tested until this phase is done.

- [X] T002 Update `request()` helper and all `NonSourceabilityRequest(…)` literals in `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/NonSourceabilityInformationManagerTest.kt` — set `currentlyActive = true` where `bypassQa = true`, `currentlyActive = false` otherwise
- [X] T003 Update all `NonSourceabilityRequest(…)` literals in `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/controller/MetaDataControllerNonSourceableTest.kt` with the same rule

**Checkpoint**: `./gradlew dataland-backend:compileTestKotlin` succeeds; all pre-existing tests pass.

---

## Phase 3: User Story 4 — Invalid Combination Rejection (Priority: P4) ⬆️ prerequisite for all stories

> US4 is implemented first because its guard (`bypassQa=false, currentlyActive=true → 400`) is a fast-path validation that runs before any other case. Implementing it first avoids writing duplicate guards in later phases.

**Goal**: Any `bypassQa=false, currentlyActive=true` request is rejected immediately with HTTP 400.

**Independent Test**: Submit `bypassQa=false, currentlyActive=true` and verify `InvalidInputApiException` is thrown.

### Tests for User Story 4

- [X] T004 [P] [US4] Add test `invalid combination bypassQa false currentlyActive true throws InvalidInputApiException` to `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/NonSourceabilityInformationManagerTest.kt`

### Implementation for User Story 4

- [X] T005 [US4] Add early-exit guard in `processNonSourceabilityRequest`

**Checkpoint**: T004 test passes; all existing tests still pass.

---

## Phase 4: User Story 3 — Standard QA Submission (Priority: P3)

**Goal**: `bypassQa=false, currentlyActive=false` (existing happy path) enforces tightened constraints — reject if active entry exists (409) or pending entry exists (409).

**Independent Test**: Submit with no existing entries → pending entry created. Submit again with active or pending entry → `ConflictApiException`.

### Tests for User Story 3

- [X] T006 [P] [US3] Add test `bypassQa false currentlyActive false rejected when active entry exists throws ConflictApiException` to `NonSourceabilityInformationManagerTest.kt`
- [X] T007 [P] [US3] Add test `bypassQa false currentlyActive false rejected when pending entry exists throws ConflictApiException` to `NonSourceabilityInformationManagerTest.kt`

### Implementation for User Story 3

- [X] T008 [US3] Replace the existing `InvalidInputApiException` duplicate check in `processNonSourceabilityRequest`

**Checkpoint**: T006, T007 pass. Existing `creates pending entry when bypassQa is false` test still passes.

---

## Phase 5: User Story 2 — Admin Bypass-Mark as Non-Sourceable (Priority: P2)

**Goal**: `bypassQa=true, currentlyActive=true` (existing admin bypass) enforces the same tightened constraints as US3.

**Independent Test**: Submit with no existing entries → accepted active entry. Submit when active entry or pending entry exists → `ConflictApiException`.

### Tests for User Story 2

- [X] T009 [P] [US2] Add test `bypassQa true currentlyActive true rejected when active entry exists throws ConflictApiException` to `NonSourceabilityInformationManagerTest.kt`
- [X] T010 [P] [US2] Add test `bypassQa true currentlyActive true rejected when pending entry exists throws ConflictApiException` to `NonSourceabilityInformationManagerTest.kt`

### Implementation for User Story 2

- [X] T011 [US2] Add the `bypassQa=true, currentlyActive=true` branch in `processNonSourceabilityRequest`

**Checkpoint**: T009, T010 pass. Existing `creates accepted active entry when bypassQa is true` test still passes.

---

## Phase 6: User Story 1 — Admin Reversal (Priority: P1) 🎯 MVP

**Goal**: `bypassQa=true, currentlyActive=false` deactivates the active entry, creates a new audit entry, returns 201-equivalent response, emits no lifecycle event.

**Independent Test**: Create active entry → submit reversal → verify old entry `currentlyActive=false`, new entry returned, `isCurrentlyActive=false`.

### Tests for User Story 1

- [X] T012 [P] [US1] Add test `bypassQa true currentlyActive false deactivates active entry and returns new entry` to `NonSourceabilityInformationManagerTest.kt`
- [X] T013 [P] [US1] Add test `bypassQa true currentlyActive false returns ConflictApiException when no active entry exists` to `NonSourceabilityInformationManagerTest.kt`
- [X] T014 [P] [US1] Add test `bypassQa true currentlyActive false returns ConflictApiException when pending entry exists` to `NonSourceabilityInformationManagerTest.kt`
- [X] T015 [P] [US1] Add test `bypassQa true currentlyActive false does not emit lifecycle event` to `NonSourceabilityInformationManagerTest.kt` (verify `cloudEventMessageHandler` not called for the reversal)

### Implementation for User Story 1

- [X] T016 [US1] Add the `bypassQa=true, currentlyActive=false` branch in `processNonSourceabilityRequest`

**Checkpoint**: T012–T015 pass. `isCurrentlyActive` returns false after reversal.

---

## Phase 7: Controller & API (All Stories)

**Purpose**: Expose the changes correctly at the HTTP layer and add controller-level validation for the reversal flow.

- [X] T017 [P] Update `@ApiResponses` on `postNonSourceabilityOfADataset` in `MetaDataApi.kt`
- [X] T018 [P] Add controller test `reversal succeeds for admin and isDataNonSourceable returns 404 afterwards`

---

## Phase 8: Test Infrastructure Migration

**Purpose**: Migrate `NonSourceabilityInformationManagerTest` from H2 + `@DirtiesContext` to `BaseIntegrationTest` (real Postgres container, `containerized-db` profile) — consistent with the established backend test pattern.

- [X] T019 Migrate `NonSourceabilityInformationManagerTest` to extend `BaseIntegrationTest`

---

## Phase 9: Polish & Cross-Cutting Concerns

- [X] T020 [P] Run `./gradlew ktlintFormat` and fix any lint issues in changed files
- [X] T021 [P] Run `./gradlew dataland-backend:test` and confirm all tests pass
- [ ] T022 Note in a PR description comment that `dataland-e2etests` callers of `NonSourceabilityRequest(…)` will need `currentlyActive` added after `./gradlew dataland-e2etests:generateClients` is run (tracked as follow-up)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** (Setup): No dependencies — start here
- **Phase 2** (Foundational): Depends on Phase 1 — unblocks all further work
- **Phase 3** (US4 guard): Depends on Phase 2 — implement first; guard is prerequisite for all other branches
- **Phase 4** (US3), **Phase 5** (US2), **Phase 6** (US1): All depend on Phase 2. Can proceed in any order after Phase 3 guard is in place. US1 is the highest business value.
- **Phase 7** (Controller/API): Depends on Phases 3–6 being complete
- **Phase 8** (Test migration): Independent of Phases 3–7; can be done any time after Phase 2
- **Phase 9** (Polish): After all above

### Parallel Opportunities per User Story

**US1 (reversal)**: T012, T013, T014, T015 can all be written in parallel (different test methods, same file). T016 depends on none of them (tests validate it, not the other way around).

**US2 + US3**: T006, T007, T009, T010 are all independent test additions — parallel. T008 and T011 touch the same method so should be sequential.

**Phase 7**: T017 and T018 are in different files — parallel.

### Implementation Strategy (MVP first)

1. T001–T003 (must-do first)
2. T005 (US4 guard — fast, then everything builds correctly)
3. T016 (US1 reversal logic — highest business value)
4. T012–T015 (US1 tests to validate)
5. T008, T011 (US3 + US2 constraint tightening)
6. T006, T007, T009, T010 (US3 + US2 tests)
7. T017, T018 (API layer)
8. T019 (test migration — low risk, independent)
9. T020–T022 (polish)
