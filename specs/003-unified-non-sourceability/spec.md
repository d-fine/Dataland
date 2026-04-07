# Feature Specification: Unified Non-Sourceability Lifecycle

**Feature Branch**: `003-unified-non-sourceability`  
**Created**: 2026-04-07  
**Status**: Draft  
**Input**: User description: "The specification introduces a unified way for evaluating the non-sourceability of datasets throughout dataland-backend, dataland-data-sourcing-service and dataland-qa-service with the single source of truth in dataland-backend."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Start a Non-Sourceability Request (Priority: P1)

As a data steward, I can mark a dataset as non-sourceable so that all related services immediately reflect that a non-sourceability review process has started.

**Why this priority**: This is the entry point for the full lifecycle. Without reliable request creation and propagation, no downstream QA or state transitions can happen.

**Independent Test**: Can be fully tested by creating a non-sourceability request for an existing dataset and verifying that backend, QA review tracking, and data sourcing workflow state are synchronized.

**Acceptance Scenarios**:

1. **Given** a dataset exists and is eligible for sourcing updates, **When** a user submits a non-sourceability request with QA enabled, **Then** a non-sourceability record is created in the backend and downstream services receive and apply the corresponding review and workflow updates.
2. **Given** a dataset exists and is eligible for sourcing updates, **When** a user submits a non-sourceability request with QA bypass enabled, **Then** the backend creates the non-sourceability record and no QA review object is created for that request.

---

### User Story 2 - Resolve Request with QA Acceptance (Priority: P2)

As a QA reviewer, I can accept a non-sourceability review so that the request is activated and the dataset transitions to a non-sourceable state across all relevant services.

**Why this priority**: Acceptance is the main path to completing the process and achieving the intended business state change.

**Independent Test**: Can be fully tested by taking a pending QA review to Accepted and verifying that backend status, active flag, and data sourcing state are all updated consistently.

**Acceptance Scenarios**:

1. **Given** a non-sourceability request is pending QA, **When** QA marks the review as Accepted with a comment, **Then** the QA review record stores the accepted decision and comment, the backend marks the request as accepted and currently active, and the data sourcing workflow changes to NonSourceable.

---

### User Story 3 - Resolve Request with QA Rejection (Priority: P3)

As a QA reviewer, I can reject a non-sourceability review so that the request outcome is recorded without activating non-sourceable status.

**Why this priority**: Rejection is needed for governance completeness and auditability, but it is secondary to the acceptance path that drives target operational state.

**Independent Test**: Can be fully tested by rejecting a pending QA review and verifying that backend decision status is updated while activation does not occur.

**Acceptance Scenarios**:

1. **Given** a non-sourceability request is pending QA, **When** QA marks the review as Rejected with a comment, **Then** the QA review record stores the rejected decision and comment, and the backend status is updated to rejected without setting the request as currently active.

### Edge Cases

- A request is submitted for a dataset that has no corresponding data sourcing object: the request is recorded, the propagation failure is surfaced for operational follow-up, and partial updates are not silently treated as success.
- A duplicate start request is submitted while another request is still open for the same dataset: the system prevents conflicting active review flows and returns a clear business error.
- A QA resolution event arrives for an unknown or already closed non-sourceability request: no state changes are applied and the event is flagged for investigation.
- Duplicate messages are delivered by asynchronous communication channels: repeated processing does not create duplicate QA records or contradictory state transitions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST create a backend non-sourceability record when a user starts a non-sourceability request for a dataset.
- **FR-002**: The backend non-sourceability record MUST be the authoritative source for the request identifier, lifecycle status, and current activity state.
- **FR-003**: When a new non-sourceability request is created with QA enabled, the system MUST publish a domain event indicating that QA review is required.
- **FR-004**: Upon receiving the "QA required" event, the QA service MUST create a corresponding QA review record linked to the originating non-sourceability request.
- **FR-005**: Upon receiving the "QA required" event, the data sourcing service MUST transition the related data sourcing workflow state to NonSourceableVerification.
- **FR-006**: When a request is created with QA bypass enabled, the system MUST skip creation of the QA review record.
- **FR-007**: The QA service MUST allow reviewers to submit a decision (Accepted or Rejected) with a review comment for a specific non-sourceability request.
- **FR-008**: When QA decision is Accepted, the QA service MUST publish a domain event describing the accepted outcome.
- **FR-009**: Upon receiving an Accepted outcome event, the backend MUST update the related non-sourceability record to qaStatus = Accepted and isCurrentlyActive = true.
- **FR-010**: Upon receiving an Accepted outcome event, the data sourcing service MUST transition the related data sourcing workflow state to NonSourceable.
- **FR-011**: When QA decision is Rejected, the QA service MUST publish a domain event describing the rejected outcome.
- **FR-012**: Upon receiving a Rejected outcome event, the backend MUST update the related non-sourceability record to qaStatus = Rejected and MUST NOT set isCurrentlyActive = true.
- **FR-013**: All lifecycle updates across services MUST be traceable by a shared non-sourceability request identifier.
- **FR-014**: The system MUST preserve the latest QA decision and reviewer comment in the QA review record for auditability.
- **FR-015**: State transitions triggered by duplicate lifecycle events MUST be idempotent.

### Key Entities *(include if feature involves data)*

- **NonSourceabilityInformation**: Canonical backend record representing a dataset non-sourceability request, including request identifier, dataset identifier, QA status, current activity flag, and lifecycle timestamps.
- **NonSourceableQaReviewInformation**: QA review record representing review status and comment for a specific non-sourceability request.
- **DataSourcingLifecycleState**: The sourcing-state view for a dataset that changes through NonSourceableVerification and NonSourceable based on non-sourceability lifecycle events.
- **NonSourceabilityLifecycleEvent**: Domain event that communicates start and resolution outcomes between backend, QA service, and data sourcing service.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 99% of valid non-sourceability start requests are reflected in all intended downstream service states within 60 seconds.
- **SC-002**: 100% of accepted QA decisions result in backend active status and non-sourceable workflow status consistency within 60 seconds.
- **SC-003**: 100% of rejected QA decisions leave requests inactive while persisting rejection status and comments.
- **SC-004**: In end-to-end UAT, at least 95% of test participants can complete start-to-resolution workflows without manual cross-service data correction.
- **SC-005**: Duplicate lifecycle events do not increase duplicate QA review records above 0 in verification tests.

## Assumptions

- Dataset identifiers are consistent and resolvable across backend, QA, and data sourcing services.
- Existing service authentication and authorization controls remain unchanged for this feature.
- Asynchronous inter-service messaging infrastructure already exists and can deliver lifecycle events between participating services.
- Data sourcing objects already support the workflow states required for verification and final non-sourceable outcomes.
- QA bypass is an intentional business path controlled by authorized users and should skip QA review creation only.
