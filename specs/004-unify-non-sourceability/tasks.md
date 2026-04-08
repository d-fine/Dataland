# Tasks: Unified Non-Sourceability Lifecycle

**Input**: Design documents from /specs/004-unify-non-sourceability/
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included because the specification explicitly requires unit/integration/e2e coverage for all behavior changes.

**Organization**: Tasks are grouped by user story so each story is independently implementable and testable.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize shared artifacts needed by all services before feature implementation.

- [ ] T001 [P] Optional project hygiene: add implementation progress notes section to specs/004-unify-non-sourceability/plan.md
- [ ] T002 Update backend API documentation entries for non-sourceability in dataland-backend/backendOpenApi.json
- [ ] T003 [P] Update QA API documentation entries for non-sourceability in dataland-qa-service/qaServiceOpenApi.json
- [ ] T004 [P] Update data-sourcing API/state documentation references in dataland-data-sourcing-service/dataSourcingServiceOpenApi.json

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared contracts, persistence foundations, and event plumbing that block all user stories.

**CRITICAL**: No user story implementation starts before this phase is complete.

- [ ] T005 Create backend non-sourceability persistence entity in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/entities/NonSourceabilityInformationEntity.kt
- [ ] T006 Create backend non-sourceability repository in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/NonSourceabilityDataRepository.kt
- [ ] T007 [P] Create backend migration for non-sourceability table and uniqueness constraints in dataland-backend/src/main/kotlin/db/migration/V6__CreateNonSourceabilityInformation.kt
- [ ] T008 Create QA non-sourceability review entity in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/entities/NonSourceableQaReviewInformationEntity.kt
- [ ] T009 [P] Create QA non-sourceability review repository in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/repositories/NonSourceableQaReviewRepository.kt
- [ ] T010 [P] Create QA migration for review table keyed by nonSourceabilityId in dataland-qa-service/src/main/kotlin/db/migration/V2__CreateNonSourceableQaReviewInformation.kt
- [ ] T011 Add shared non-sourceability event payload model in dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/model/NonSourceabilityLifecycleEvent.kt
- [ ] T012 [P] Add shared event type enum for non-sourceability lifecycle in dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/model/NonSourceabilityEventType.kt
- [ ] T013 Implement idempotent event deduplication utility in dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/services/EventDeduplicationService.kt
- [ ] T054 [P] Add unit tests for EventDeduplicationService idempotency guarantees in dataland-message-queue-utils/src/test/kotlin/org/dataland/datalandmessagequeueutils/services/EventDeduplicationServiceTest.kt
- [ ] T014 Wire correlation-id logging helper for non-sourceability flows in dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/logging/CorrelationLogging.kt
- [ ] T055 [P] Add unit tests for CorrelationLogging verifying correlation ID is appended to MDC context in dataland-message-queue-utils/src/test/kotlin/org/dataland/datalandmessagequeueutils/logging/CorrelationLoggingTest.kt
- [ ] T044 [P] Add backend listener tests for malformed or unresolvable nonSourceabilityId discard-and-log behavior in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/NonSourceabilityQaDecisionConsumerTest.kt
- [ ] T045 [P] Add data-sourcing listener tests for malformed or unresolvable nonSourceabilityId discard-and-log behavior in dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/NonSourceabilityEventConsumerTest.kt *(Creates test file; extended by T018)*
- [ ] T046 Implement backend listener fail-fast validation and discard-with-error-log handling for malformed or unknown nonSourceabilityId in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/NonSourceabilityQaDecisionListener.kt
- [ ] T047 Implement data-sourcing listener fail-fast validation and discard-with-error-log handling for malformed or unknown nonSourceabilityId in dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/NonSourceabilityEventListener.kt

**Checkpoint**: Foundations complete, user stories can proceed in priority order.

---

## Phase 3: User Story 1 - Request Non-Sourceability With QA Review (Priority: P1) 🎯 MVP

**Goal**: Submit non-sourceability requests via backend, create canonical backend record, fan out creation event to QA/data-sourcing, and support bypass fast-path.

**Independent Test**: Submit POST /metadata/nonSourceable with bypassQa false and true, then verify backend canonical state plus downstream QA/data-sourcing effects.

### Tests for User Story 1

- [ ] T015 [P] [US1] Add backend controller tests for GET/POST/HEAD nonSourceable endpoints in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/controller/MetaDataControllerNonSourceableTest.kt
- [ ] T016 [P] [US1] Add backend service tests for duplicate tuple rejection and bypass authorization in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt
- [ ] T017 [P] [US1] Add QA listener tests for non-sourceability-created event ingestion in dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/NonSourceabilityEventListenerTest.kt
- [ ] T018 [P] [US1] Add data-sourcing listener tests for transition to NonSourceableVerification and NonSourceable in dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/NonSourceabilityEventConsumerTest.kt *(Extends test file created by T045)*

### Implementation for User Story 1

- [ ] T019 [US1] Rewire active sourceability write/read logic to canonical non-sourceability model in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt while keeping SourceabilityEntity as backup-only
- [ ] T020 [US1] Update non-sourceability search filter to qaStatus/currentlyActive semantics in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/repositories/utils/NonSourceableDataSearchFilter.kt
- [ ] T021 [US1] Rewire backend metadata endpoints GET /metadata/nonSourceable, POST /metadata/nonSourceable, and HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod} to canonical model in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/controller/MetaDataController.kt
- [ ] T022 [US1] Update non-sourceability API contract signatures and models in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/api/MetaDataApi.kt
- [ ] T023 [US1] Emit non-sourceability-created and non-sourceability-auto-accepted events from backend workflow in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManager.kt
- [ ] T024 [US1] Implement QA consumer for non-sourceability-created event and create pending review rows in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/NonSourceabilityEventListener.kt
- [ ] T025 [US1] Implement data-sourcing consumer transitions for created/auto-accepted events in dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/NonSourceabilityEventListener.kt
- [ ] T048 [US1] Ensure backend endpoints and processors do not use SourceabilityEntity as runtime source and add explicit backup-only guard checks in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/SourceabilityDataManagerTest.kt

**Checkpoint**: User Story 1 is independently functional and demonstrable.

---

## Phase 4: User Story 2 - QA Accepts Non-Sourceability (Priority: P2)

**Goal**: Record QA acceptance and propagate accepted state to backend canonical record and data-sourcing state.

**Independent Test**: From a pending non-sourceability review, post Accepted decision and verify backend becomes active plus data-sourcing transitions to NonSourceable.

### Tests for User Story 2

- [ ] T026 [P] [US2] Add QA controller tests for accepted decision on POST /nonSourceable/{nonSourceabilityId} in dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/controller/NonSourceabilityQaControllerTest.kt
- [ ] T027 [P] [US2] Add backend consumer tests for non-sourceability-qa-accepted event application in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/NonSourceabilityQaDecisionConsumerTest.kt *(Extends test class created by T044)*
- [ ] T028 [P] [US2] Add data-sourcing consumer tests for accepted-event transition to NonSourceable in dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/NonSourceabilityQaAcceptedConsumerTest.kt
- [ ] T049 [P] [US2] Add QA controller tests for GET /nonSourceable and GET /nonSourceable/queue filtering and pending queue behavior in dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/controller/NonSourceabilityQaControllerTest.kt

### Implementation for User Story 2

- [ ] T029 [US2] Implement QA non-sourceability decision endpoint scaffold for POST /nonSourceable/{nonSourceabilityId} handling both Accepted and Rejected routing at the controller layer (accepted-path persistence implemented here; T036 completes the rejected branch) in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/controller/NonSourceabilityQaController.kt
- [ ] T050 [US2] Implement QA listing endpoint GET /nonSourceable for non-sourceability review records in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/controller/NonSourceabilityQaController.kt
- [ ] T051 [US2] Implement QA queue endpoint GET /nonSourceable/queue for pending review records in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/controller/NonSourceabilityQaController.kt
- [ ] T030 [US2] Implement QA decision manager with accepted-event emission in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/NonSourceabilityQaReviewManager.kt
- [ ] T031 [US2] Implement backend consumer updating qaStatus Accepted and currentlyActive true in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/NonSourceabilityQaDecisionListener.kt
- [ ] T032 [US2] Implement data-sourcing accepted-event transition to NonSourceable in dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/NonSourceabilityQaDecisionListener.kt

**Checkpoint**: User Story 2 acceptance flow is independently functional.

---

## Phase 5: User Story 3 - QA Rejects Non-Sourceability (Priority: P3)

**Goal**: Record QA rejection and propagate rejected state while keeping data-sourcing in NonSourceableVerification for manual handling.

**Independent Test**: From a pending review, post Rejected decision and verify backend inactive/rejected while data-sourcing remains NonSourceableVerification.

### Tests for User Story 3

- [ ] T033 [P] [US3] Add QA controller tests for rejected decision persistence and comment handling in dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/controller/NonSourceabilityQaControllerTest.kt
- [ ] T034 [P] [US3] Add backend consumer tests for non-sourceability-qa-rejected handling in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/services/NonSourceabilityQaDecisionConsumerTest.kt
- [ ] T035 [P] [US3] Add data-sourcing consumer tests verifying rejection keeps NonSourceableVerification in dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/NonSourceabilityQaRejectedConsumerTest.kt
- [ ] T052 [P] [US3] Add data-sourcing controller tests for PATCH /data-sourcing/{dataSourcingId}/state role checks and non-sourceability state transitions in dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/DataSourcingControllerTest.kt

### Implementation for User Story 3

- [ ] T036 [US3] Extend QA decision manager to emit non-sourceability-qa-rejected event in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/NonSourceabilityQaReviewManager.kt
- [ ] T037 [US3] Extend backend listener to persist qaStatus Rejected and currentlyActive false in dataland-backend/src/main/kotlin/org/dataland/datalandbackend/services/NonSourceabilityQaDecisionListener.kt
- [ ] T038 [US3] Extend data-sourcing listener to keep NonSourceableVerification and mark manual handling in dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/NonSourceabilityQaDecisionListener.kt
- [ ] T053 [US3] Update PATCH /data-sourcing/{dataSourcingId}/state non-sourceability transition and authorization handling in dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/SecurityUtilsService.kt (authorization checks) and dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/controller/DataSourcingController.kt (state-machine routing for NonSourceableVerification and NonSourceable)

**Checkpoint**: User Story 3 rejection flow is independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final compatibility, observability, and end-to-end validation across stories.

- [ ] T039 [P] Update backend non-sourceability OpenAPI contract notes in specs/004-unify-non-sourceability/contracts/backend-metadata-nonsourceable.openapi.md
- [ ] T040 [P] Update QA non-sourceability OpenAPI contract notes in specs/004-unify-non-sourceability/contracts/qa-nonsourceable.openapi.md
- [ ] T041 [P] Update messaging compatibility and idempotency notes in specs/004-unify-non-sourceability/contracts/nonsourceability-messaging.md
- [ ] T042 Run end-to-end quickstart validation scenarios and capture outcomes in specs/004-unify-non-sourceability/quickstart.md *(Note: QTR-002 cross-service e2e coverage is satisfied by per-service integration tests in T015–T018, T026–T028, and T033–T035; this task captures observational SLO outcomes from quickstart scenarios)*
- [ ] T043 Run coverage and regression test suites for all touched services via gradle and record results in specs/004-unify-non-sourceability/plan.md

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1): no dependencies.
- Foundational (Phase 2): depends on Setup completion and blocks all user stories.
- User Story phases (Phase 3-5): each depends on Foundational completion; execute in priority order for incremental delivery.
- Polish (Phase 6): depends on completion of selected user stories.

### User Story Dependencies

- US1 (P1): starts immediately after Foundational; no dependency on other user stories.
- US2 (P2): depends on US1 event and persistence foundations but can be implemented once US1 baseline endpoints/events exist.
- US3 (P3): depends on US2 decision infrastructure and shares same QA decision endpoint/listener stack.

### Within Each User Story

- Tests are written first and must fail before implementation.
- Entity/repository and event-model changes precede manager/controller/listener behavior.
- Backend canonical updates and consumer transitions complete before story checkpoint.

## Parallel Opportunities

- Phase 1 tasks T001, T003, and T004 can run in parallel with T002.
- In Phase 2, T007, T009, T010, T012, T044, T045, T054, and T055 can run in parallel after T005/T008 kick off.
- For US1, tests T015-T018 are parallelizable; implementation T024 and T025 can proceed in parallel after backend event contract work.
- For US2 and US3, service-specific endpoint/controller tests and implementations are parallelizable across QA and data-sourcing once shared contracts are in place.
- Polish tasks T039-T041 are parallelizable.

## Parallel Example: User Story 1

- Parallel test lane:
  - T015 in dataland-backend/src/test/kotlin/org/dataland/datalandbackend/controller/MetaDataControllerNonSourceableTest.kt
  - T017 in dataland-qa-service/src/test/kotlin/org/dataland/datalandqaservice/services/NonSourceabilityEventListenerTest.kt
  - T018 in dataland-data-sourcing-service/src/test/kotlin/org/dataland/datasourcingservice/serviceTests/NonSourceabilityEventConsumerTest.kt
- Parallel implementation lane:
  - T024 in dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/services/NonSourceabilityEventListener.kt
  - T025 in dataland-data-sourcing-service/src/main/kotlin/org/dataland/datasourcingservice/services/NonSourceabilityEventListener.kt

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Deliver Phase 3 (US1) with all tests green.
3. Validate independent test criteria for US1 from quickstart.
4. Demo/deploy MVP slice.

### Incremental Delivery

1. Deliver US1 baseline lifecycle request flow.
2. Add US2 accepted decision propagation.
3. Add US3 rejected decision propagation.
4. Run final polish and cross-service regression validation.

### Parallel Team Strategy

1. Team A: backend canonical model and API/tasks in Phase 2-3.
2. Team B: QA review persistence, listener, and decision endpoint in Phase 2-5.
3. Team C: data-sourcing event consumers/state transitions and authorization tests in Phase 3-5.
4. Joint hardening: Phase 6 contracts, quickstart execution, and coverage verification.
