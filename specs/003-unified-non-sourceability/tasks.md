# Tasks: Unified Non-Sourceability Lifecycle

**Input**: Design documents from `/specs/003-unified-non-sourceability/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/, quickstart.md

**Tests**: Tests are required for this feature by `QTR-001` through `QTR-005` in the specification.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup

**Purpose**: Align shared contracts and messaging surfaces before service implementation starts.

- [X] T001 Update shared non-sourceability message constants in `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/constants/MessageType.kt`
- [X] T002 [P] Update routing and queue constants for non-sourceability lifecycle events in `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/constants/RoutingKeyNames.kt` and `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/constants/QueueNames.kt`
- [X] T003 [P] Align shared message payloads for backend and QA event exchange in `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/messages/SourceabilityMessage.kt` and `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/messages/QaStatusChangeMessage.kt`
- [X] T004 [P] Finalize feature contracts in `specs/003-unified-non-sourceability/contracts/backend-api.yaml`, `specs/003-unified-non-sourceability/contracts/qa-service-api.yaml`, `specs/003-unified-non-sourceability/contracts/datasourcing-api.yaml`, and `specs/003-unified-non-sourceability/contracts/EVENTS.md`

---

## Phase 2: Foundational

**Purpose**: Shared persistence and authorization prerequisites required by all user stories.

**⚠️ CRITICAL**: No user story work should begin until this phase is complete.

- [X] T005 Create backend migration for the authoritative non-sourceability table in `dataland-backend/src/main/kotlin/db/migration/V13__CreateNonSourceabilityInformationTable.kt`
- [X] T006 [P] Create QA-service migration for non-sourceability review persistence in `dataland-qa-service/src/main/kotlin/db/migration/V12__CreateNonSourceableQaReviewTable.kt`
- [X] T007 [P] Introduce `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/entities/NonSourceabilityInformationEntity.kt` alongside the existing `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/entities/SourceabilityEntity.kt`; keep `SourceabilityEntity.kt` and its table untouched to preserve existing data for a future out-of-scope migration task
- [X] T008 [P] Introduce backend repository support for the new entity in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/NonSourceabilityDataRepository.kt` and adapt `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/SourceabilityDataRepository.kt`
- [X] T009 [P] Introduce QA persistence and query scaffolding in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/entities/NonSourceableQaReviewInformationEntity.kt`, `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/repositories/NonSourceableQaReviewRepository.kt`, and `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/model/NonSourceableQaReviewInformation.kt`
- [X] T010 [P] Update data-sourcing patch authorization rules in `dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/SecurityUtilsService.kt`

**Checkpoint**: Shared message contracts, database foundations, and state-patch authorization are ready.

---

## Phase 3: User Story 1 - Start a Non-Sourceability Request (Priority: P1) 🎯 MVP

**Goal**: Users can create non-sourceability requests in the backend and trigger synchronized QA-review and data-sourcing state updates.

**Independent Test**: Submit `POST /metadata/nonSourceable` for both `bypassQa=false` and `bypassQa=true`, then verify backend persistence, QA review creation/absence, and data-sourcing state transitions.

### Tests for User Story 1

- [X] T011 [P] [US1] Add backend endpoint and service tests for `GET/POST/HEAD /metadata/nonSourceable` in `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/controller/MetaDataControllerTest.kt` and `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`
- [X] T012 [P] [US1] Add QA created-event ingestion tests in `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/QaEventListenerQaServiceTest.kt` and `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/repository/NonSourceableQaReviewRepositoryTest.kt`
- [X] T013 [P] [US1] Add data-sourcing state transition tests for created and auto-accepted events in `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/DataSourcingWorkflowTest.kt` and `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/unitTests/DataSourcingManagerTest.kt`
- [X] T014 [P] [US1] Add cross-service idempotency coverage for start-request events in `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`, `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/QaEventListenerQaServiceTest.kt`, and `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/DataSourcingWorkflowTest.kt`

### Implementation for User Story 1

- [X] T015 [US1] Rework backend request and response models to `NonSourceabilityInformation` semantics in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/metainformation/SourceabilityInfo.kt` and `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/metainformation/SourceabilityInfoResponse.kt`
- [X] T016 [US1] Rewire backend query, create, duplicate-check, and `currentlyActive` logic in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt`
- [X] T017 [US1] Update metadata endpoint behavior to the new backend semantics in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/controller/MetaDataController.kt`
- [X] T018 [P] [US1] Update backend non-sourceability search support in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/utils/NonSourceableDataSearchFilter.kt` and `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/SourceabilityDataRepository.kt`
- [X] T019 [P] [US1] Publish `non-sourceability-created` and `non-sourceability-auto-accepted` events from backend workflows in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt`; ensure `nonSourceabilityId` and event type are logged for traceability
- [X] T020 [P] [US1] Implement QA review creation on backend created-events in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/QaEventListenerQaService.kt` and `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/NonSourceableQaReviewManager.kt`
- [X] T021 [P] [US1] Add non-sourceability review query endpoints in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/controller/QaController.kt`
- [X] T022 [P] [US1] Implement data-sourcing transitions for created and auto-accepted events in `dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/DataSourcingServiceListener.kt` and `dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/DataSourcingManager.kt`; if dataSourcingId is unknown, log error with correlation ID and dead-letter the message for investigation

**Checkpoint**: Creating a non-sourceability request works end to end, including duplicate protection, QA-review creation, bypass flow, and `NonSourceableVerification`/`NonSourceable` transitions.

---

## Phase 4: User Story 2 - Resolve Request with QA Acceptance (Priority: P2)

**Goal**: QA reviewers can accept pending non-sourceability reviews and activate the request across backend and data-sourcing service.

**Independent Test**: Seed or create a pending review, call `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Accepted`, then verify accepted review persistence, backend activation, and data-sourcing promotion to `NonSourceable`.

### Tests for User Story 2

- [X] T023 [P] [US2] Add QA acceptance endpoint tests in `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/controller/QaControllerTest.kt` and `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/QaReviewManagerQaChangeTest.kt`
- [X] T024 [P] [US2] Add backend acceptance-event consumer tests in `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`
- [X] T025 [P] [US2] Add data-sourcing acceptance transition tests in `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/DataSourcingWorkflowTest.kt`
- [X] T026 [P] [US2] Add idempotent acceptance replay tests in `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/QaReviewManagerQaChangeTest.kt`, `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`, and `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/unitTests/DataSourcingManagerTest.kt`

### Implementation for User Story 2

- [X] T027 [US2] Introduce the controller endpoint skeleton for `POST /nonSourceable/{nonSourceabilityId}` in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/controller/QaController.kt` that dispatches to acceptance or rejection handlers based on qaStatus parameter; implement accepted-review persistence and reviewer capture in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/QaReviewManager.kt`
- [X] T028 [P] [US2] Emit `QaNonSourceabilityAcceptedEvent` from QA service in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/QaReviewManager.kt` and `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/messages/QaStatusChangeMessage.kt`; log `nonSourceabilityId` and event type for traceability
- [X] T029 [P] [US2] Consume accepted QA events in backend and set `qaStatus=Accepted` plus `currentlyActive=true` in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt` and `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityQaEventListener.kt`; log `nonSourceabilityId`, event type, and state change for traceability
- [X] T030 [P] [US2] Consume accepted QA events in data-sourcing service and promote state to `NonSourceable` in `dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/DataSourcingServiceListener.kt`; log `nonSourceabilityId` and state transition for traceability; if dataSourcingId is unknown, log error and dead-letter

**Checkpoint**: QA acceptance independently works from API input through event propagation to backend activation and final data-sourcing state.

---

## Phase 5: User Story 3 - Resolve Request with QA Rejection (Priority: P3)

**Goal**: QA reviewers can reject pending non-sourceability reviews without activating the request or promoting the dataset to final non-sourceable state.

**Independent Test**: Seed or create a pending review, call `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Rejected`, then verify rejected review persistence, backend status update, and retained `NonSourceableVerification` state.

### Tests for User Story 3

- [X] T031 [P] [US3] Add QA rejection endpoint tests in `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/controller/QaControllerTest.kt` and `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/QaReviewManagerQaChangeTest.kt`
- [X] T032 [P] [US3] Add backend rejection-event consumer tests in `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`
- [X] T033 [P] [US3] Add data-sourcing rejection retention tests in `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/DataSourcingWorkflowTest.kt`
- [X] T034 [P] [US3] Add idempotent rejection replay tests in `dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/QaReviewManagerQaChangeTest.kt`, `dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt`, and `dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/unitTests/DataSourcingManagerTest.kt`

### Implementation for User Story 3

- [X] T035 [US3] Extend the controller endpoint introduced in T027 to handle rejection path in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/controller/QaController.kt`; implement rejected-review persistence and API handling in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/QaReviewManager.kt`
- [X] T036 [P] [US3] Emit `QaNonSourceabilityRejectedEvent` from QA service in `dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/QaReviewManager.kt` and `dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/messages/QaStatusChangeMessage.kt`; log `nonSourceabilityId` and event type for traceability
- [X] T037 [P] [US3] Consume rejected QA events in backend and persist `qaStatus=Rejected` while keeping `currentlyActive=false` in `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt` and `dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityQaEventListener.kt`; log `nonSourceabilityId`, event type, and status update for traceability
- [X] T038 [P] [US3] Consume rejected QA events in data-sourcing service without promoting beyond `NonSourceableVerification` in `dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/DataSourcingServiceListener.kt`; log `nonSourceabilityId`, event type, and retained state for traceability; flag unrecognized events for investigation

**Checkpoint**: QA rejection independently works and preserves the manual follow-up state without activating the backend record.

---

## Phase 7: Cross-Service E2E Validation

**Purpose**: Validate complete end-to-end workflows across all three services in coordinated scenarios.

- [X] T044 [P] Add cross-service end-to-end test scenarios in `dataland-e2etests/` covering both complete workflows: (1) start request with bypassQa=false → QA accept → backend activation → data-sourcing promotion to NonSourceable, and (2) start request with bypassQa=false → QA reject → backend status update → data-sourcing retention in NonSourceableVerification. Tests MUST verify message order, idempotency under replay, and error handling for missing/invalid records.

**Purpose**: Final validation, documentation alignment, and repository quality gates.

- [X] T045 [P] Update developer and contract documentation in `specs/003-unified-non-sourceability/quickstart.md`, `specs/003-unified-non-sourceability/data-model.md`, and `specs/003-unified-non-sourceability/plan.md`
- [X] T046 Run backend tests and repository checks for changed backend files using `dataland-backend/build.gradle.kts`
- [X] T047 Run QA-service tests and repository checks for changed QA files using `dataland-qa-service/build.gradle.kts`
- [X] T048 Run data-sourcing tests and repository checks for changed data-sourcing files using `dataland-data-sourcing-service/build.gradle.kts`
- [X] T049 [P] Run repository linters and static checks required for commit readiness across touched modules using the existing Gradle and commit-time tooling configuration in `build.gradle.kts` and related service build files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately; defines shared constants, payloads, and contracts.
- **Foundational (Phase 2)**: Depends on Setup; blocks all user-story implementation until persistence and authorization scaffolding exist.
- **User Story 1 (Phase 3)**: Depends on Foundational; delivers the MVP start-request flow.
- **User Story 2 (Phase 4)**: Depends on Foundational; can be implemented after US1 or in parallel once pending-review fixtures exist.
- **User Story 3 (Phase 5)**: Depends on Foundational; can be implemented after US1 or in parallel once pending-review fixtures exist.
- **E2E Validation (Phase 6)**: Depends on all three user stories being substantially complete; validates integrated workflows.
- **Polish (Phase 7)**: Depends on E2E validation and desired user stories being complete.

### User Story Dependencies

- **US1 (P1)**: No dependency on other user stories after Foundational.
- **US2 (P2)**: Uses the same foundational persistence and event scaffolding; independent validation can seed pending review records if US1 is not yet complete.
- **US3 (P3)**: Uses the same foundational persistence and event scaffolding; independent validation can seed pending review records if US1 is not yet complete.

### Within Each User Story

- Write tests first and confirm they fail before implementation.
- Implement persistence/model changes before controller/service wiring.
- Implement event emission before consumer-side assertions.
- Finish idempotency handling before closing the story.

### Suggested Story Completion Order

1. Complete Setup and Foundational phases.
2. Deliver US1 as the MVP.
3. Deliver US2 for accepted-review completion.
4. Deliver US3 for rejected-review completion.
5. Finish Phase 6 quality gates and documentation.

---

## Parallel Opportunities

- T002, T003, and T004 can run in parallel during Setup.
- T006 through T010 can run in parallel during Foundational work.
- US1 test tasks T011 through T014 can run in parallel.
- US1 implementation tasks T018 through T022 can run in parallel after T015 through T017 establish backend semantics.
- US2 test tasks T023 through T026 can run in parallel.
- US2 implementation tasks T028 through T030 can run in parallel after T027 establishes QA acceptance handling.
- US3 test tasks T031 through T034 can run in parallel.
- US3 implementation tasks T036 through T038 can run in parallel after T035 establishes QA rejection handling.
- T039 and T043 can run in parallel with the final verification tasks once implementation is stable.

---

## Parallel Example: User Story 1

```text
T011 Add backend endpoint and service tests for GET/POST/HEAD /metadata/nonSourceable
T012 Add QA created-event ingestion tests
T013 Add data-sourcing state transition tests for created and auto-accepted events
T014 Add cross-service idempotency coverage for start-request events
```

```text
T018 Update backend non-sourceability search support
T019 Publish non-sourceability-created and non-sourceability-auto-accepted events from backend workflows
T020 Implement QA review creation on backend created-events
T022 Implement data-sourcing transitions for created and auto-accepted events
```

## Parallel Example: User Story 2

```text
T023 Add QA acceptance endpoint tests
T024 Add backend acceptance-event consumer tests
T025 Add data-sourcing acceptance transition tests
T026 Add idempotent acceptance replay tests
```

```text
T028 Emit QaNonSourceabilityAcceptedEvent from QA service
T029 Consume accepted QA events in backend
T030 Consume accepted QA events in data-sourcing service
```

## Parallel Example: User Story 3

```text
T031 Add QA rejection endpoint tests
T032 Add backend rejection-event consumer tests
T033 Add data-sourcing rejection retention tests
T034 Add idempotent rejection replay tests
```

```text
T036 Emit QaNonSourceabilityRejectedEvent from QA service
T037 Consume rejected QA events in backend
T038 Consume rejected QA events in data-sourcing service without promotion
```

---

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2.
2. Deliver Phase 3 (US1) only.
3. Validate the full start-request flow, including bypass and non-bypass behavior.
4. Demo or merge the MVP if the team wants incremental delivery.

### Incremental Delivery

1. Setup + Foundational.
2. US1 start-request lifecycle.
3. US2 acceptance lifecycle.
4. US3 rejection lifecycle.
5. Phase 6 lint, test, and documentation cleanup.

### Parallel Team Strategy

1. One engineer handles shared message utilities and backend scaffolding.
2. One engineer handles QA persistence and decision endpoints.
3. One engineer handles data-sourcing listeners and authorization updates.
4. Integrate via the shared event contracts and finish with end-to-end validation.

---

## Notes

- `[P]` tasks touch different files or can be validated independently.
- User-story tasks are labeled `[US1]`, `[US2]`, and `[US3]` for traceability back to the specification.
- The task list assumes contract-first updates remain in sync with code changes.
- Repository linters and static checks are mandatory exit criteria for this feature.