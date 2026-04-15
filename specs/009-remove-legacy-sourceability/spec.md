# Feature Specification: Remove Legacy Sourceability System

**Feature Branch**: `009-remove-legacy-sourceability`  
**Created**: 2026-04-14  
**Status**: Draft  
**Input**: User description: "Remove the old sourceability code paths from backend services, message queue bindings, and tests. The new NonSourceabilityInformationManager-based lifecycle is the canonical system and remains untouched."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Remove Legacy Sourceability Backend Code (Priority: P1)

As a platform maintainer, I want the legacy append-only sourceability event log code removed from the backend so that only the canonical QA-reviewed non-sourceability lifecycle remains, reducing confusion and maintenance burden.

**Why this priority**: The legacy backend code (manager, entity, repository, search filter, info/response types) is the core of the old system. Removing it eliminates the primary source of confusion between the two coexisting systems and prevents accidental use of the deprecated path.

**Independent Test**: Can be verified by confirming that the backend compiles and all existing new-system tests pass after removal. The legacy API endpoints should no longer be accessible while the new non-sourceability endpoints continue to function normally.

**Acceptance Scenarios**:

1. **Given** the backend codebase contains both legacy and new sourceability systems, **When** the legacy sourceability manager, entity, repository, search filter, and info/response types are removed, **Then** the backend compiles successfully with no references to the removed components.
2. **Given** the legacy sourceability code has been removed, **When** all new-system non-sourceability tests are executed, **Then** they pass without modification.
3. **Given** the legacy sourceability code has been removed, **When** the MetaDataController is examined, **Then** it no longer has a dependency on the removed sourceability manager.

---

### User Story 2 - Remove Legacy Community Manager Listener (Priority: P2)

As a platform maintainer, I want the legacy message listener that processed `SourceabilityMessage` on the old routing key removed from the community manager so that only the new-system listeners handle non-sourceability events.

**Why this priority**: The legacy listener consumes messages on a routing key that no new producer writes to. Removing it eliminates dead code and the risk of it interfering with the new event-driven lifecycle. The associated overload method on the request update manager that only this listener calls should also be removed.

**Independent Test**: Can be verified by confirming the community manager compiles, the new-system listeners (`processNonSourceabilityAutoAcceptedEvent`, `processNonSourceabilityQaAcceptedEvent`) still function, and the removed listener's queue binding no longer exists.

**Acceptance Scenarios**:

1. **Given** the community manager has both a legacy listener and two new-system listeners, **When** the legacy listener processing `SourceabilityMessage` on routing key `DATA_NONSOURCEABLE` is removed, **Then** the community manager compiles and starts successfully.
2. **Given** the legacy listener is removed, **When** the two new-system listeners are tested, **Then** they continue to process events correctly.
3. **Given** the legacy listener is removed, **When** the `SourceabilityMessage`-accepting overload of the request patching method is removed, **Then** the community manager compiles because no code references it.

---

### User Story 3 - Remove Legacy Message Queue Constants (Priority: P3)

As a platform maintainer, I want the legacy routing key constant and message type removed from the shared message queue utilities so that the message contract reflects only the active system, while preserving the shared exchange and message types still in use by other services.

**Why this priority**: After removing the legacy listeners and producers, the routing key constant and message type become unused. Removing them prevents future developers from creating new code against the deprecated contract. The shared exchange must be preserved because the new system uses it.

**Independent Test**: Can be verified by confirming that all services compile after the constants are removed and that no remaining code references the removed constants.

**Acceptance Scenarios**:

1. **Given** the legacy `DATA_NONSOURCEABLE` message type and routing key constants exist, **When** they are removed, **Then** all services compile with no unresolved references.
2. **Given** the legacy queue name for the community manager's old listener exists, **When** it is removed, **Then** all services compile with no unresolved references.
3. **Given** the shared exchange `BACKEND_DATA_NONSOURCEABLE` exists, **When** the legacy constants are removed, **Then** the exchange is preserved and the new system continues publishing and consuming through it.

---

### User Story 4 - Clean Up Legacy Tests (Priority: P4)

As a platform maintainer, I want tests that exercise only the legacy system removed or adapted so that the test suite remains green and accurately reflects the current system, without losing coverage for the new non-sourceability lifecycle.

**Why this priority**: Tests for removed code will fail after the code is deleted. Cleaning them up ensures CI remains green. Tests that partially overlap (testing both old and new paths) need surgical adaptation rather than wholesale deletion.

**Independent Test**: Can be verified by running the full test suite after all code removals and confirming all tests pass.

**Acceptance Scenarios**:

1. **Given** a test class that exclusively tests the removed sourceability manager, **When** it is deleted, **Then** the test suite passes.
2. **Given** a test class that has both legacy and new-system test methods for the community manager listener, **When** only the legacy test methods are deleted, **Then** the new-system test methods continue to pass.
3. **Given** test methods that call the removed `SourceabilityMessage` overload, **When** they are adapted to use the retained `(companyId, dataType, reportingPeriod)` overload, **Then** the tests pass and cover the same logical behavior.
4. **Given** test data providers that supply dummy legacy sourceability objects, **When** they are removed, **Then** no remaining test references them.
5. **Given** end-to-end or component tests that reference legacy queue names or legacy non-sourceable test cases, **When** they are removed or cleaned up, **Then** the remaining test suite passes.

---

### Edge Cases

- What happens if a message is still in-flight on the legacy routing key when the listener is removed? — Since no producer writes to the legacy routing key in production, this scenario does not arise. Any stale messages in the queue will remain undelivered (dead-lettered or ignored) and can be purged manually if needed.
- What happens if the `data_sourceability` table is queried after the entity code is removed? — The table is intentionally preserved for future data migration; it simply becomes an orphan table with no active readers until a separate migration task addresses it.
- What happens if a downstream service still references a removed constant? — The build will fail at compile time, surfacing the issue immediately. This is the desired safety mechanism.
- What happens if a test for the new system accidentally imports a removed legacy type? — The build will fail at compile time, requiring the test to be updated to use only new-system types.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The legacy sourceability manager, entity class, repository, search filter, and info/response types MUST be removed from the backend service.
- **FR-002**: The MetaDataController MUST no longer depend on the removed sourceability manager.
- **FR-003**: The legacy listener in the community manager that processes `SourceabilityMessage` on the old routing key MUST be removed.
- **FR-004**: The `SourceabilityMessage`-accepting overload of the request-patching method in the community manager MUST be removed.
- **FR-005**: The legacy `DATA_NONSOURCEABLE` message type and routing key constants MUST be removed from the shared message queue utilities.
- **FR-006**: The legacy queue name for the community manager's old non-sourceability listener MUST be removed if it is defined as a shared constant.
- **FR-007**: The shared exchange (`BACKEND_DATA_NONSOURCEABLE`) MUST be preserved — it is actively used by the new system.
- **FR-008**: The `SourceabilityMessage` class MUST be preserved — it is still used by the data-sourcing to user-service notification path.
- **FR-009**: The `data_sourceability` database table MUST NOT be dropped — it is reserved for a future data migration.
- **FR-010**: All new-system non-sourceability components (manager, listeners, events, QA workflow) MUST remain unchanged and fully functional.
- **FR-011**: The data-sourcing service and user-service MUST NOT be modified — their existing notification path remains active.
- **FR-012**: Tests that exclusively exercise the removed legacy code MUST be deleted.
- **FR-013**: Tests that partially exercise legacy code alongside new-system code MUST be adapted to remove only the legacy portions while preserving new-system coverage.
- **FR-014**: Frontend production code MUST NOT be changed — legacy frontend cleanup is a separate task.
- **FR-015**: All services MUST compile and pass their test suites after the removal is complete.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Zero references to the removed legacy sourceability manager, entity, repository, search filter, or info/response types exist in the codebase after removal (verified by full-text search).
- **SC-002**: All backend, community-manager, and message-queue-utils services compile successfully after the changes.
- **SC-003**: 100% of existing new-system tests (non-sourceability lifecycle, QA review, event listeners, E2E tests) pass without modification.
- **SC-004**: The full CI test suite passes with no new failures introduced by this change.
- **SC-005**: The `data_sourceability` table remains present and unmodified in the database schema.
- **SC-006**: The `BACKEND_DATA_NONSOURCEABLE` exchange remains configured and functional for the new system's message routing.
- **SC-007**: The data-sourcing → user-service notification path (using `SourceabilityMessage`) continues to function correctly.
- **SC-008**: Net reduction in backend code by removing all legacy sourceability classes (manager, entity, repository, filter, info types, legacy listener, legacy overload, and associated test code).

## Assumptions

- The new `NonSourceabilityInformationManager`-based lifecycle is fully deployed and operational; no traffic flows through the legacy path in production.
- No external systems or third-party integrations depend on the legacy sourceability API endpoints or message routing keys.
- The `data_sourceability` table does not have active foreign key constraints that would cause issues when the entity code is removed (the table simply becomes unmanaged).
- The `SourceabilityMessage` type used by `DataSourcingManager.performStatePatch()` is structurally independent of the legacy constants being removed (it can be constructed without `MessageType.DATA_NONSOURCEABLE`).
- Frontend pages that reference legacy sourceability endpoints may break after this removal — this is accepted and will be addressed in a separate frontend cleanup task.
- The message broker (RabbitMQ) will not fail if a declared queue (`community-manager.queue.nonSourceableData`) has no active consumer — stale queues can be purged manually or via broker management.
