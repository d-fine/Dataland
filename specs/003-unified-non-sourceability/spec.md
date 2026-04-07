# Feature Specification: Unified Non-Sourceability Lifecycle

**Feature Branch**: `003-unified-non-sourceability`  
**Created**: 2026-04-07  
**Status**: Draft  
**Input**: User description: "The specification introduces a unified way for evaluating the non-sourceability of datasets throughout dataland-backend, dataland-data-sourcing-service and dataland-qa-service with the single source of truth in dataland-backend."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Start a Non-Sourceability Request (Priority: P1)

As an authenticated user, I can mark a dataset as non-sourceable so that all related services immediately reflect that a non-sourceability review process has started.

**Why this priority**: This is the entry point for the full lifecycle. Without reliable request creation and propagation, no downstream QA or state transitions can happen.

**Independent Test**: Can be fully tested by creating a non-sourceability request for an existing dataset and verifying that backend, QA review tracking, and data sourcing workflow state are synchronized.

**Acceptance Scenarios**:

1. **Given** a dataset exists and no non-sourceability entry is recorded, **When** a user sends a valid non-sourceability request with `bypassQa = false`, **Then** the backend creates a `NonSourceabilityInformation` object with `currentlyActive = false` and `qaStatus = Pending`, emits a non-sourceability-created event, the QA service creates a `NonSourceableQaReviewInformation` record linked to the entry, and the data-sourcing service transitions the corresponding sourcing object to `NonSourceableVerification`.
2. **Given** a non-sourceability entry for the same `(companyId, dataType, reportingPeriod)` already exists and is active, **When** a duplicate request is submitted, **Then** the backend rejects the request with an appropriate error response and no new entries are created.
3. **Given** a valid non-sourceability request with `bypassQa = true`, **When** submitted, **Then** the backend creates the entry with `currentlyActive = true` and `qaStatus = Accepted` immediately, no QA review record is created, and the data-sourcing service transitions the dataset to `NonSourceable` directly.

---

### User Story 2 - Resolve Request with QA Acceptance (Priority: P2)

As a QA reviewer, I can accept a non-sourceability review so that the request is activated and the dataset transitions to a non-sourceable state across all relevant services.

**Why this priority**: Acceptance is the main path to completing the process and achieving the intended business state change.

**Independent Test**: Can be fully tested by taking a pending QA review to Accepted and verifying that backend status, active flag, and data sourcing state are all updated consistently.

**Acceptance Scenarios**:

1. **Given** a `NonSourceableQaReviewInformation` with `qaStatus = Pending`, **When** a QA reviewer submits an acceptance via `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus = Accepted`, **Then** the QA service updates the review record, emits a QA-accepted event, the backend sets `qaStatus = Accepted` and `currentlyActive = true` on the corresponding `NonSourceabilityInformation`, and the data-sourcing service transitions the sourcing object to `NonSourceable`.
2. **Given** the QA-accepted event is received by both the backend and the data-sourcing service, **When** either service has already processed the event, **Then** reprocessing the same event is idempotent and does not create duplicate state changes.

---

### User Story 3 - Resolve Request with QA Rejection (Priority: P3)

As a QA reviewer, I can reject a non-sourceability review so that the request outcome is recorded without activating non-sourceable status.

**Why this priority**: Rejection is needed for governance completeness and auditability, but it is secondary to the acceptance path that drives target operational state.

**Independent Test**: Can be fully tested by rejecting a pending QA review and verifying that backend decision status is updated while activation does not occur.

**Acceptance Scenarios**:

1. **Given** a `NonSourceableQaReviewInformation` with `qaStatus = Pending`, **When** a QA reviewer submits a rejection via `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus = Rejected`, **Then** the QA service updates the review record, emits a QA-rejected event, the backend sets `qaStatus = Rejected` on the corresponding `NonSourceabilityInformation` entry while leaving `currentlyActive = false`.
2. **Given** the QA-rejected event is processed by the backend, **When** the data-sourcing service receives the event, **Then** the sourcing object remains in `NonSourceableVerification` and is manually handled by the QA-Team.

### Edge Cases

- A request is submitted for a dataset that has no corresponding data sourcing object: the request is recorded in the backend, an error is logged with the `nonSourceabilityId`, dataset identifiers, and timestamp, and the message is dead-lettered for manual investigation. The backend records the attempt and surfaces it as an operational alert (e.g., monitoring/alerting system or dead-letter queue inspection). Partial state updates are not treated as success; message handlers are idempotent and do not corrupt state.
- A duplicate start request is submitted while another request is still open for the same dataset: the system prevents conflicting active review flows and returns a clear business error (409 Conflict).
- A QA resolution event arrives for an unknown or already closed non-sourceability request: no state changes are applied, an error is logged with correlation ID and event details, and the message is dead-lettered for investigation (no silent drops).
- Duplicate messages are delivered by asynchronous communication channels: repeated processing does not create duplicate QA records or contradictory state transitions (idempotent handling required in all listeners).

## Requirements *(mandatory)*

### Functional Requirements

**Request Creation & Validation**

- **FR-001**: The backend MUST expose a `POST /metadata/nonSourceable` endpoint that accepts a non-sourceability request with parameters: `companyId`, `dataType`, `reportingPeriod`, `reason`, and `bypassQa` flag.
- **FR-002**: When a non-sourceability request is received, the backend MUST check if an active non-sourceability entry already exists for the same `(companyId, dataType, reportingPeriod)` tuple; if yes, the request MUST be rejected with an appropriate conflict error.
- **FR-003**: The backend MUST create a `NonSourceabilityInformation` record with `currentlyActive = false` and `qaStatus = Pending` when `bypassQa = false`.
- **FR-004**: The backend MUST create a `NonSourceabilityInformation` record with `currentlyActive = true` and `qaStatus = Accepted` immediately when `bypassQa = true`, bypassing the QA review process.
- **FR-005**: When a non-sourceability request is created with `bypassQa = false`, the backend MUST emit a non-sourceability-created event containing the `nonSourceabilityId`, request details, and initial state.
- **FR-006**: When a non-sourceability request is created with `bypassQa = true`, the backend MUST emit a non-sourceability-auto-accepted event.

**QA Review Workflow**

- **FR-007**: Upon receiving a non-sourceability-created event, the QA service MUST create a `NonSourceableQaReviewInformation` record with `qaStatus = Pending` linked to the originating `nonSourceabilityId`.
- **FR-008**: The QA service MUST expose a `POST /nonSourceable/{nonSourceabilityId}` endpoint that accepts QA review decisions with parameters: `qaStatus` (Accepted or Rejected) and optional `qaComment`.
- **FR-009**: When a QA reviewer submits a decision via `POST /nonSourceable/{nonSourceabilityId}`, the QA service MUST update the corresponding `NonSourceableQaReviewInformation` record.
- **FR-010**: When QA decision is "Accepted", the QA service MUST emit a QA-accepted event containing the `nonSourceabilityId` and decision state.
- **FR-011**: When QA decision is "Rejected", the QA service MUST emit a QA-rejected event containing the `nonSourceabilityId` and decision state.
- **FR-012**: The QA service MUST preserve the reviewer user ID and QA comment in the review record for auditability.

**Backend Processing of QA Decisions**

- **FR-013**: Upon receiving a QA-accepted event, the backend MUST update the corresponding `NonSourceabilityInformation` record to set `qaStatus = Accepted` and `currentlyActive = true`.
- **FR-014**: Upon receiving a QA-rejected event, the backend MUST update the corresponding `NonSourceabilityInformation` record to set `qaStatus = Rejected` while keeping `currentlyActive = false`.

**Data Sourcing Service Integration**

- **FR-015**: Upon receiving a non-sourceability-created event (when `bypassQa = false`), the data sourcing service MUST transition the corresponding dataset sourcing object state to `NonSourceableVerification`.
- **FR-016**: Upon receiving a non-sourceability-auto-accepted event (when `bypassQa = true`), the data sourcing service MUST transition the corresponding dataset sourcing object state directly to `NonSourceable`.
- **FR-017**: Upon receiving a QA-accepted event, the data sourcing service MUST transition the corresponding dataset sourcing object state from `NonSourceableVerification` to `NonSourceable`.
- **FR-018**: Upon receiving a QA-rejected event, the data sourcing service MUST leave the dataset sourcing object in `NonSourceableVerification` state for manual handling by the QA Team.

**Cross-Service Consistency**

- **FR-019**: All lifecycle events (non-sourceability-created, non-sourceability-auto-accepted, QA-accepted, QA-rejected) MUST include the `nonSourceabilityId` as a correlation identifier so consumers can unambiguously link records.
- **FR-020**: State transitions triggered by duplicate lifecycle events MUST be idempotent—reprocessing the same event MUST not create duplicate records or trigger contradictory state changes.
- **FR-021**: The backend MUST be the authoritative source for the `NonSourceabilityInformation` object and its lifecycle state, with the QA service and data sourcing service acting as event consumers that maintain derived state.

### Contract and Messaging Requirements *(mandatory for cross-service changes)*

- **CMR-001**: The `NonSourceabilityInformation` schema published by the backend and the `NonSourceableQaReviewInformation` schema used by the QA service MUST be documented in OpenAPI 3.x. Any changes MUST note backward compatibility expectations and impacted consumers (QA service, data-sourcing service).
- **CMR-002**: The non-sourceability-created event, non-sourceability-auto-accepted event, QA-accepted event, and QA-rejected event schemas MUST be documented. New fields MUST be additive; removing or renaming fields is a breaking change requiring coordinated rollout.
- **CMR-003**: The `nonSourceabilityId` correlation identifier MUST be present on all events and REST payloads so that consumers can unambiguously link records across services.
- **CMR-004**: Event delivery semantics MUST be at-most-once via RabbitMQ. RabbitMQ manages message persistence and redelivery on broker-side failures. Services consuming events MUST implement idempotent event handlers to ensure that redelivered or replayed messages do not cause duplicate records or contradictory state transitions.
- **CMR-005**: HTTP error responses returned by `POST /metadata/nonSourceable` and `POST /nonSourceable/{nonSourceabilityId}` MUST follow Dataland's established API error response conventions (status codes, error body format), ensuring consistency with other Dataland endpoints and simplifying client error handling.


### Quality and Test Requirements *(mandatory)*

- **QTR-001**: Every behavior change in dataland-backend, dataland-qa-service, and dataland-data-sourcing-service MUST include unit or integration tests at the level where the behavior is introduced.
- **QTR-002**: The end-to-end non-sourceability flows (request → QA accepted → active, and request → QA rejected → manual QA handling in `NonSourceableVerification`) MUST each be covered by integration or end-to-end tests.
- **QTR-003**: Changed modules MUST maintain at least 80% line coverage and MUST NOT reduce existing coverage baselines.
- **QTR-004**: Idempotency MUST be verified for event handlers in all three services by replaying the same lifecycle event multiple times in test scenarios and confirming that no duplicate records are created and state is not corrupted.
- **QTR-005**: All changed code in the affected services MUST pass the linters and static checks already enforced by the repository tooling, including checks that run during local commit workflows where configured.

### Security and Operations Requirements *(mandatory)*

- **SOR-001**: The `bypassQa` flag MUST be restricted to users with an elevated role (e.g., admin). Requests using `bypassQa = true` without the required role MUST be rejected with an authorization error. Primary use case is testing; emergency override usages in production MUST be logged with the requesting user ID, timestamp, dataset identifiers, and justification comment for audit trail.
- **SOR-002**: Access to `POST /metadata/nonSourceable` and `POST /nonSourceable/{nonSourceabilityId}` MUST enforce authentication and role-based access control consistent with existing Dataland API security policies.
- **SOR-003**: The endpoints MUST be subject to Dataland's standard API rate limiting and throttling policies to prevent abuse and ensure operational stability.

### Key Entities

- **NonSourceabilityInformation** (owned by dataland-backend): Represents the authoritative record that a dataset has been flagged as non-sourceable. Key attributes: `nonSourceabilityId`, `companyId`, `dataType`, `reportingPeriod`, `reason`, `bypassQa`, `qaStatus` (Pending / Accepted / Rejected), `currentlyActive`, `uploadTime`, `uploaderUserId`.
- **NonSourceableQaReviewInformation** (owned by dataland-qa-service): Represents the QA review task for a non-sourceability claim. Key attributes: `nonSourceabilityId` (foreign reference to backend record), `companyId`, `dataType`, `reportingPeriod`, `qaStatus`, `reason`, `uploaderUserId`, `uploadTime`, `reviewerUserId`, `qaComment`.
- **DataSourcingObject** (owned by dataland-data-sourcing-service): Tracks the sourcing lifecycle of a dataset. This feature reuses the existing states `NonSourceableVerification` (pending QA outcome) and `NonSourceable` (confirmed non-sourceable).


## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 99% of valid non-sourceability start requests are reflected in all intended downstream service states within 60 seconds.
- **SC-002**: 100% of accepted QA decisions result in backend active status and non-sourceable workflow status consistency within 60 seconds.
- **SC-003**: 100% of rejected QA decisions leave requests inactive while persisting rejection status and comments.
- **SC-004**: In end-to-end UAT, at least 95% of test participants can complete start-to-resolution workflows without manual cross-service data correction.
- **SC-005**: Duplicate lifecycle events do not increase duplicate QA review records above 0 in verification tests.
- **SC-006**: For requests submitted with QA required (bypassQa = false), the corresponding QA review task is visible to QA reviewers (e.g., in the QA service task queue) with P99 latency of 30 seconds.

## Assumptions

- Dataset identifiers are consistent and resolvable across backend, QA, and data sourcing services.
- Existing service authentication and authorization controls remain unchanged for this feature.
- Asynchronous inter-service messaging infrastructure already exists and can deliver lifecycle events between participating services.
- Data sourcing objects already support the workflow states required for verification and final non-sourceable outcomes.
- QA bypass is an intentional business path primarily used for testing scenarios, with optional emergency override capability controlled by authorized users. All emergency override usages MUST be logged for audit and compliance.

## Clarifications

### Session 2026-04-07

- Q: What event delivery semantics are required for lifecycle events between backend, QA, and data-sourcing services? → A: At-most-once via RabbitMQ (Option B). RabbitMQ handles message persistence and retry logic; services consume events exactly once and MUST implement idempotent handlers to avoid duplicate side effects if a message is redelivered by RabbitMQ or reprocessed due to consumer restart.
- Q: What is the SLA for QA review task visibility when a non-sourceability request is submitted? → A: Within 30 seconds (P99 latency). QA teams must have timely visibility of new review tasks to maintain operational responsiveness.
- Q: What rate limiting or throttling strategy applies to the non-sourceability endpoints? → A: Use existing Dataland default rate limits (Option A). The new endpoints inherit standard API guards already in place across the platform to minimize implementation overhead.
- Q: What is the business intent of the bypassQa flag? → A: Primarily for testing, sometimes emergency override (Option D). The feature is designed to reduce QA overhead during testing scenarios; may also be used as an emergency exception path in production when a dataset requires immediate non-sourceability status without waiting for QA review. All emergency uses MUST be logged for audit and compliance.
- Q: How should API error responses be formatted for the non-sourceability endpoints? → A: Inherit Dataland API conventions (Option B). The endpoints follow existing Dataland error response formats to maintain API consistency and reduce implementation overhead.
