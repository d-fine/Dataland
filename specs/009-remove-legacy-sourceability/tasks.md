# Tasks: Remove Legacy Sourceability System

**Input**: Design documents from `/specs/009-remove-legacy-sourceability/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md

**Tests**: No new tests are written. Legacy-only tests are deleted; partially-legacy tests are adapted. New-system tests are preserved unchanged.

**Organization**: Tasks are grouped by user story to enable independent implementation and verification of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Verification Baseline)

**Purpose**: Confirm baseline compiles before making any changes

- [X] T001 Verify baseline compilation of all affected services: `./gradlew dataland-backend:compileKotlin dataland-community-manager:compileKotlin dataland-message-queue-utils:compileKotlin`
- [X] T002 Verify baseline tests pass: `./gradlew dataland-backend:test dataland-community-manager:test`

**Checkpoint**: Baseline is green — all removals start from a known-good state

---

## Phase 2: User Story 1 — Remove Legacy Sourceability Backend Code (Priority: P1) 🎯 MVP

**Goal**: Delete the legacy sourceability manager, entity, repository, filter, and info/response types from the backend. Remove the dormant dependency from MetaDataController. Clean up dangling KDoc reference.

**Independent Test**: `./gradlew dataland-backend:compileKotlin dataland-backend:test` — backend compiles and all new-system tests pass.

### Implementation for User Story 1

- [X] T003 [P] [US1] Delete legacy service file `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt`
- [X] T004 [P] [US1] Delete legacy entity file `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/entities/SourceabilityEntity.kt`
- [X] T005 [P] [US1] Delete legacy repository file `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/SourceabilityDataRepository.kt`
- [X] T006 [P] [US1] Delete legacy search filter file `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/utils/NonSourceableDataSearchFilter.kt`
- [X] T007 [P] [US1] Delete legacy model file `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/metainformation/SourceabilityInfo.kt`
- [X] T008 [P] [US1] Delete legacy response model file `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/metainformation/SourceabilityInfoResponse.kt`
- [X] T009 [US1] Remove dormant `sourceabilityDataManager` dependency from `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/controller/MetaDataController.kt` — remove the `@Autowired val sourceabilityDataManager: SourceabilityDataManager` constructor parameter (line 47), the `import org.dataland.datalandbackend.services.SourceabilityDataManager` (line 16), and the KDoc `@param sourceabilityDataManager ...` line (line 38)
- [X] T010 [US1] Update KDoc in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/NonSourceabilityInformationManager.kt` — remove the sentence `[SourceabilityDataManager] is retained as backup-only.` from the class-level comment (line 28) to fix the dangling reference
- [X] T011 [US1] Delete legacy test file `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`
- [X] T012 [US1] Verify backend compiles and tests pass: `./gradlew dataland-backend:compileKotlin dataland-backend:test`

**Checkpoint**: Backend is clean — all legacy sourceability classes are gone, new-system tests pass unchanged

---

## Phase 3: User Story 2 — Remove Legacy Community Manager Listener (Priority: P2)

**Goal**: Remove the legacy `processMessageForDataReportedAsNonSourceable()` listener and its two private helpers from `CommunityManagerListener`. Remove the `SourceabilityMessage`-accepting overload from `DataRequestUpdateManager`. New-system listeners remain untouched.

**Independent Test**: `./gradlew dataland-community-manager:compileKotlin dataland-community-manager:test` — community-manager compiles and new-system listener tests pass.

### Implementation for User Story 2

- [X] T013 [US2] Remove the legacy listener method `processMessageForDataReportedAsNonSourceable()` and its `@RabbitListener` annotation from `dataland-community-manager/src/main/kotlin/org/dataland/datalandcommunitymanager/services/CommunityManagerListener.kt` — this is the block from the `@RabbitListener(bindings = [...Queue("community-manager.queue.nonSourceableData"...])` annotation through the end of the `processMessageForDataReportedAsNonSourceable` method body
- [X] T014 [US2] Remove the two private legacy helper methods from `dataland-community-manager/src/main/kotlin/org/dataland/datalandcommunitymanager/services/CommunityManagerListener.kt` — `checkThatReceivedDataIsComplete(SourceabilityMessage)` and `checkThatDatasetWasSetToNonSourceable(SourceabilityMessage)`. Also remove the now-unused imports: `SourceabilityMessage`, `MessageType.DATA_NONSOURCEABLE` (if only used by removed code), and `RoutingKeyNames.DATA_NONSOURCEABLE`
- [X] T015 [US2] Remove the legacy `patchAllNonWithdrawnRequestsToStatusNonSourceable(sourceabilityInfo: SourceabilityMessage, correlationId: String)` overload from `dataland-community-manager/src/main/kotlin/org/dataland/datalandcommunitymanager/services/DataRequestUpdateManager.kt` — this is the `@Transactional` method at approximately lines 393–407. Also remove the now-unused `SourceabilityMessage` import if no other code references it
- [X] T016 [US2] Verify community-manager compiles: `./gradlew dataland-community-manager:compileKotlin`

**Checkpoint**: Community manager is clean — legacy listener and overload are gone, new-system listeners compile and function normally

---

## Phase 4: User Story 3 — Remove Legacy Message Queue Constants (Priority: P3)

**Goal**: Remove the dead `DATA_NONSOURCEABLE` message type and routing key constants from the shared message-queue-utils. Verify no remaining consumers reference them.

**Independent Test**: `./gradlew dataland-message-queue-utils:compileKotlin dataland-backend:compileKotlin dataland-community-manager:compileKotlin` — all services that previously referenced the constants still compile.

### Implementation for User Story 3

- [X] T017 [P] [US3] Remove `const val DATA_NONSOURCEABLE = "Data non-sourceable"` from `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/constants/MessageType.kt` (line 19)
- [X] T018 [P] [US3] Remove `const val DATA_NONSOURCEABLE = "dataNonSourceable"` from `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/constants/RoutingKeyNames.kt` (line 22)
- [X] T019 [US3] Verify all affected services compile after constant removal: `./gradlew dataland-message-queue-utils:compileKotlin dataland-backend:compileKotlin dataland-community-manager:compileKotlin`

**Checkpoint**: Shared messaging constants are clean — only new-system constants remain. Exchange `BACKEND_DATA_NONSOURCEABLE` preserved.

---

## Phase 5: User Story 4 — Clean Up Legacy Tests (Priority: P4)

**Goal**: Remove or adapt all remaining tests that reference legacy code: community-manager listener unit tests, data request update manager tests, test data providers, frontend component test, and RabbitMQ admin test.

**Independent Test**: `./gradlew dataland-community-manager:test` plus frontend lint/typecheck — all tests pass with no legacy references.

### Implementation for User Story 4

- [X] T020 [P] [US4] Remove 3 legacy test methods from `dataland-community-manager/src/test/kotlin/org/dataland/datalandcommunitymanager/services/CommunityManagerListenerUnitTest.kt` — delete: (1) `valid nonsourceable message should be processed successfully` (approx lines 155–169), (2) `should throw exception for incomplete data in nonsourceable message` (approx lines 222–251), (3) `should throw exception when isNonSourceable is false in nonsourceable message` (approx lines 252–269). Also remove the unused field `typeNonSourceable` and the `SourceabilityMessage` import if no longer referenced by remaining code
- [X] T021 [P] [US4] Remove 2 legacy helper methods from `dataland-community-manager/src/test/kotlin/org/dataland/datalandcommunitymanager/utils/DataRequestUpdateManagerTestDataProvider.kt` — delete `getDummyNonSourceableInfo()` (approx lines 23–34) and `getDummySourceableInfo()` (approx lines 36–45). Also remove the `SourceabilityMessage` import (line 13)
- [X] T022 [US4] Adapt tests in `dataland-community-manager/src/test/kotlin/org/dataland/datalandcommunitymanager/services/DataRequestUpdateManagerTest.kt`: (1) Delete test `validate that providing information about a dataset that is sourceable throws an IllegalArgumentException` (approx lines 375–382) — this validation only existed in the removed legacy overload. (2) Adapt test `validate that notification behaviour is as expected when requests are patched from Open to NonSourceable` (approx line 387) — change call from `patchAllNonWithdrawnRequestsToStatusNonSourceable(dummyNonSourceableInfo, correlationId)` to `patchAllNonWithdrawnRequestsToStatusNonSourceable(companyId = testDataProvider.dummyCompanyId, dataTypeAsString = testDataProvider.nuclearAndGas, reportingPeriod = "dummyPeriod", correlationId = correlationId, requestStatusChangeReason = testDataProvider.dummyRequestChangeReason)`. (3) Adapt test `validate that patching corresponding requests for a dataset only processes the corresponding requests` (approx line 414) — same calling convention change. (4) Remove the `dummyNonSourceableInfo` and `dummySourceableInfo` field declarations (lines 74–75)
- [X] T023 [P] [US4] Remove the nonSourceable test case from `dataland-frontend/tests/component/components/pages/ViewDataRequestPageLegacy.cy.ts` — delete the entire `it('Check viewDataRequest page for nonSourceable request renders as expected and reopen data request', ...)` block (approx lines 210–238)
- [X] T024 [P] [US4] Remove the legacy queue name `'community-manager.queue.nonSourceableData'` from the expected queues array in `dataland-frontend/tests/e2e/specs/admin-tools/RabbitMQAdmin.ts` (line 23)
- [X] T025 [US4] Verify all tests pass after cleanup: `./gradlew dataland-backend:test dataland-community-manager:test`

**Checkpoint**: Test suite is green — no legacy references remain in any test file

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation across all services and codebase-wide verification

- [X] T026 Run codebase-wide search to verify zero references to deleted classes: `grep -r "SourceabilityDataManager\|SourceabilityEntity\|SourceabilityDataRepository\|NonSourceableDataSearchFilter" --include="*.kt" . | grep -v build/ | grep -v .gradle/`
- [X] T027 Run full cross-service compilation: `./gradlew dataland-backend:compileKotlin dataland-community-manager:compileKotlin dataland-message-queue-utils:compileKotlin dataland-user-service:compileKotlin dataland-data-sourcing-service:compileKotlin`
- [X] T028 Run quickstart.md full verification procedure from `specs/009-remove-legacy-sourceability/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (US1 — Backend)**: Depends on Phase 1 baseline verification
- **Phase 3 (US2 — Community Manager)**: Depends on Phase 1 baseline verification. Can run in parallel with Phase 2 (different service)
- **Phase 4 (US3 — MQ Constants)**: Depends on **both** Phase 2 AND Phase 3 completion — the constants are still referenced by code removed in those phases
- **Phase 5 (US4 — Tests)**: Depends on Phase 2 and Phase 3 (tests reference removed code). T020/T021 can start after Phase 3. T022 depends on Phase 3 (overload removal). T023/T024 have no backend dependencies and can start anytime after Phase 1.
- **Phase 6 (Polish)**: Depends on all previous phases

### User Story Dependencies

- **US1 (Backend)**: Standalone — no dependency on other user stories
- **US2 (Community Manager)**: Standalone — no dependency on US1 (different service)
- **US3 (MQ Constants)**: Depends on US1 + US2 — constants are referenced by consumers removed in those stories
- **US4 (Tests)**: Depends on US1 + US2 + US3 — tests reference all removed artifacts

### Within User Stories

- File deletions within a phase (tasks marked [P]) can all run in parallel
- Modifications must happen before compile verification
- Compile verification is the final task in each user story phase

### Parallel Opportunities

**Batch 1** (after Phase 1):
- Phase 2 tasks T003–T011 (backend deletions + modifications) — all [P] file deletions in parallel, then modifications
- Phase 3 tasks T013–T015 (community-manager modifications) — can run in parallel with Phase 2

**Batch 2** (after Phases 2 + 3):
- Phase 4 tasks T017–T018 (MQ constant removals) — both [P], run in parallel

**Batch 3** (after Phase 4):
- Phase 5 tasks T020, T021, T023, T024 — all [P], run in parallel
- Phase 5 task T022 depends on T021 (shared test data provider)

---

## Parallel Example: User Story 1

```bash
# All file deletions can run simultaneously:
Task T003: Delete SourceabilityDataManager.kt
Task T004: Delete SourceabilityEntity.kt
Task T005: Delete SourceabilityDataRepository.kt
Task T006: Delete NonSourceableDataSearchFilter.kt
Task T007: Delete SourceabilityInfo.kt
Task T008: Delete SourceabilityInfoResponse.kt

# Then modifications (depend on deletions being committed):
Task T009: Modify MetaDataController.kt
Task T010: Update NonSourceabilityInformationManager.kt KDoc
Task T011: Delete SourceabilityDataManagerTest.kt

# Then verify:
Task T012: Compile + test
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Verify baseline
2. Complete Phase 2: Remove all legacy backend classes
3. **STOP and VALIDATE**: Backend compiles, all backend tests pass
4. This alone eliminates the core legacy system

### Incremental Delivery

1. Complete Phase 2 → Backend clean (MVP!)
2. Complete Phase 3 → Community manager clean
3. Complete Phase 4 → MQ constants clean
4. Complete Phase 5 → Test suite clean
5. Complete Phase 6 → Cross-cutting validation
6. Each phase makes the codebase incrementally cleaner without breaking the previous state

### Parallel Team Strategy

With two developers:
1. Both verify baseline (Phase 1)
2. Developer A: Phase 2 (backend) | Developer B: Phase 3 (community-manager)
3. Together: Phase 4 (MQ constants — unblocked by both)
4. Split Phase 5 test cleanup tasks (all marked [P] can be divided)
5. Together: Phase 6 final validation

---

## Notes

- This is a **pure removal task** — no new production code is written
- The `data_sourceability` database table is intentionally left in place (FR-009)
- The `SourceabilityMessage` class is intentionally preserved (FR-008)
- The `BACKEND_DATA_NONSOURCEABLE` exchange is intentionally preserved (FR-007)
- Frontend production code is intentionally not changed (FR-014)
- After all phases, the only remaining "sourceability" references should be: `SourceabilityMessage` (preserved), `NonSourceabilityInformationManager` (new system), and auto-generated frontend clients in `build/` (regenerated on next build)
