# Feature Specification: Unified Non-Sourceability Lifecycle

**Feature Branch**: `004-unify-non-sourceability`  
**Created**: 2026-04-08  
**Status**: Draft  
**Input**: User description: "The specification introduces a unified way for evaluating the non-sourceability of datasets throughout dataland-backend, dataland-data-sourcing-service and dataland-qa-service with the single source of truth being in the dataland-backend."

## Clarifications

### Session 2026-04-02

- Q: What should happen in data-sourcing when QA rejects a non-sourceability request? -> A: On QA rejection, dataset remains in `NonSourceableVerification` and requires manual QA-team handling.
- Q: What should be the canonical uniqueness key for non-sourceability requests? -> A: Use `(companyId, dataType, reportingPeriod)` as the canonical uniqueness key.
- Q: What should be the end-to-end propagation target from backend emit to both consumers updated? -> A: 60 seconds.
- Q: For `bypassQa = true`, which event contract should the backend emit? -> A: Emit a dedicated `non-sourceability-auto-accepted` event type.
- Q: Which delivery semantics should be required for asynchronous flows? -> A: Require at-least-once delivery with idempotent consumers.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Request Non-Sourceability With QA Review (Priority: P1)

A user to mark a specific dataset as non-sourceable. They submit a non-sourceability request via the backend. Because the `bypassQa` flag is not set, the request enters QA review. The data-sourcing service is notified and places the dataset into a pending verification state so that no further sourcing attempts are made while the review is in progress.

**Why this priority**: This is the primary happy path for the entire feature. All other stories depend on a non-sourceability entry existing in the system.

**Independent Test**: Can be fully tested by submitting a non-sourceability request without `bypassQa` and verifying that the backend stores the entry, the QA service has a corresponding review record, and the data-sourcing service shows the dataset in `NonSourceableVerification` state.

**Acceptance Scenarios**:

1. **Given** a dataset exists and no non-sourceability entry is recorded, **When** a user sends a valid non-sourceability request with `bypassQa = false`, **Then** the backend creates a `NonSourceabilityInformation` object with `currentlyActive = false` and `qaStatus = Pending`, emits a non-sourceability-created event, the QA service creates a `NonSourceableQaReviewInformation` record linked to the entry, and the data-sourcing service transitions the corresponding sourcing object to `NonSourceableVerification`.
2. **Given** a non-sourceability entry for the same `(companyId, dataType, reportingPeriod)` already exists and is active, **When** a duplicate request is submitted, **Then** the backend rejects the request with an appropriate error response and no new entries are created.
3. **Given** a valid non-sourceability request with `bypassQa = true`, **When** submitted, **Then** the backend creates the entry with `currentlyActive = true` and `qaStatus = Accepted` immediately, no QA review record is created, and the data-sourcing service transitions the dataset to `NonSourceable` directly.

---

### User Story 2 - QA Accepts Non-Sourceability (Priority: P2)

A QA reviewer examines the pending non-sourceability review and determines it is valid. They record an "Accepted" decision. The backend is notified and activates the non-sourceability entry. The data-sourcing service learns the dataset is now definitively non-sourceable and updates accordingly.

**Why this priority**: Without acceptance propagation the dataset remains stuck in `NonSourceableVerification` indefinitely. This story closes the acceptance branch of the workflow.

**Independent Test**: Can be fully tested by seeding a `NonSourceableQaReviewInformation` record in `Pending` state, issuing an acceptance decision via the QA service endpoint, and verifying the backend entry shows `currentlyActive = true` and `qaStatus = Accepted`, and the data-sourcing object is in `NonSourceable` state.

**Acceptance Scenarios**:

1. **Given** a `NonSourceableQaReviewInformation` with `qaStatus = Pending`, **When** a QA reviewer submits an acceptance via `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus = Accepted`, **Then** the QA service updates the review record, emits a QA-accepted event, the backend sets `qaStatus = Accepted` and `currentlyActive = true` on the corresponding `NonSourceabilityInformation`, and the data-sourcing service transitions the sourcing object to `NonSourceable`.
2. **Given** the QA-accepted event is received by both the backend and the data-sourcing service, **When** either service has already processed the event, **Then** reprocessing the same event is idempotent and does not create duplicate state changes.

---

### User Story 3 - QA Rejects Non-Sourceability (Priority: P3)

A QA reviewer examines the pending non-sourceability review and determines it is not valid. They record a "Rejected" decision. The backend is notified and updates the QA status on the entry. The dataset remains in `NonSourceableVerification` and is manually handled by the QA team.

**Why this priority**: Without a rejection path the system has no way to resume sourcing when a non-sourceability claim is found invalid.

**Independent Test**: Can be fully tested by seeding a `NonSourceableQaReviewInformation` record in `Pending` state, issuing a rejection via the QA service endpoint, and verifying the backend entry shows `qaStatus = Rejected` and `currentlyActive = false`.

**Acceptance Scenarios**:

1. **Given** a `NonSourceableQaReviewInformation` with `qaStatus = Pending`, **When** a QA reviewer submits a rejection via `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus = Rejected`, **Then** the QA service updates the review record, emits a QA-rejected event, the backend sets `qaStatus = Rejected` on the corresponding `NonSourceabilityInformation` entry while leaving `currentlyActive = false`.
2. **Given** the QA-rejected event is processed by the backend, **When** the data-sourcing service receives the event, **Then** the sourcing object remains in `NonSourceableVerification` and is manually handled by the QA-Team.

---

### Edge Cases

- What happens when the non-sourceability-created event is emitted but the QA service is temporarily unavailable? The event must be retained (durable messaging) and processed once the service recovers, without the backend emitting a duplicate event.
- What happens when `bypassQa = true` is used but the caller lacks the required privileges? The backend must reject the request with an authorization error and not persist any entry.
- What happens when a QA decision event references a `nonSourceabilityId` that does not exist in the backend? The backend must log the anomaly, discard the event, and not alter any state.
- What happens when a new non-sourceability request is submitted for a dataset that already has a `Rejected` entry? The system should allow a fresh entry to be created, treating each request as an independent record.
- What happens when the data-sourcing service event listener fails after the backend has already applied a state change? The sourcing service must be able to re-consume replayed events and apply changes idempotently.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to submit a non-sourceability request for a dataset via the backend, resulting in a persisted `NonSourceabilityInformation` object.
- **FR-002**: When `bypassQa = true`, the backend MUST immediately activate the non-sourceability entry (`currentlyActive = true`, `qaStatus = Accepted`) without creating a QA review record and MUST emit a dedicated `non-sourceability-auto-accepted` event that causes the data-sourcing object to transition to `NonSourceable`.
- **FR-003**: When `bypassQa = false` (default), the backend MUST emit a non-sourceability-created event after persisting the entry, with `currentlyActive = false` and `qaStatus = Pending`.
- **FR-004**: The QA service MUST listen to the non-sourceability-created event and create a corresponding `NonSourceableQaReviewInformation` record linked to the originating `nonSourceabilityId`.
- **FR-005**: The data-sourcing service MUST listen to the non-sourceability-created event and transition the corresponding sourcing object to `NonSourceableVerification` state.
- **FR-006**: The QA service MUST expose an endpoint (`POST /nonSourceable/{nonSourceabilityId}`) that allows recording an acceptance or rejection decision with an optional comment.
- **FR-007**: Upon a QA acceptance decision, the QA service MUST emit a QA-accepted event. The backend MUST listen to this event and set `qaStatus = Accepted` and `currentlyActive = true` on the corresponding entry.
- **FR-008**: Upon receiving a QA-accepted event, the data-sourcing service MUST transition the sourcing object to `NonSourceable` state.
- **FR-009**: Upon a QA rejection decision, the QA service MUST emit a QA-rejected event. The backend MUST listen to this event and set `qaStatus = Rejected` on the corresponding entry while keeping `currentlyActive = false`.
- **FR-010**: Upon receiving a QA-rejected event, the data-sourcing service MUST keep the sourcing object in `NonSourceableVerification` state and expose it for manual QA-team handling.
- **FR-011**: The backend MUST be the single source of truth for all `NonSourceabilityInformation` data; the QA service and data-sourcing service MUST NOT maintain their own authoritative copies of that data.
- **FR-012**: All event-driven state transitions MUST be idempotent so that replayed or duplicate events do not produce incorrect state.
- **FR-013**: The backend MUST reject duplicate non-sourceability requests when an active entry already exists for the same `(companyId, dataType, reportingPeriod)` tuple.

### Contract and Messaging Requirements *(mandatory for cross-service changes)*

- **CMR-001**: The `NonSourceabilityInformation` schema published by the backend and the `NonSourceableQaReviewInformation` schema used by the QA service MUST be documented in OpenAPI 3.x. Any changes MUST note backward compatibility expectations and impacted consumers (QA service, data-sourcing service).
- **CMR-002**: The non-sourceability-created event, non-sourceability-auto-accepted event, QA-accepted event, and QA-rejected event schemas MUST be documented. New fields MUST be additive; removing or renaming fields is a breaking change requiring coordinated rollout.
- **CMR-003**: The `nonSourceabilityId` correlation identifier MUST be present on all events and REST payloads so that consumers can unambiguously link records across services.
- **CMR-004**: Asynchronous delivery semantics MUST be treated as at-least-once; consumers MUST implement idempotent processing so replayed or duplicate events do not create inconsistent state.

### Quality and Test Requirements *(mandatory)*

- **QTR-001**: Every behavior change in dataland-backend, dataland-qa-service, and dataland-data-sourcing-service MUST include unit or integration tests at the level where the behavior is introduced.
- **QTR-002**: The end-to-end non-sourceability flows (request → QA accepted → active, and request → QA rejected → manual QA handling in `NonSourceableVerification`) MUST each be covered by integration or end-to-end tests.
- **QTR-003**: Changed modules MUST maintain at least 80% line coverage and MUST NOT reduce existing coverage baselines.

### Security and Operations Requirements *(mandatory)*

- **SOR-001**: The `bypassQa` flag MUST be restricted to users with an elevated role (e.g., admin). Requests using `bypassQa = true` without the required role MUST be rejected with an authorization error.
- **SOR-002**: All asynchronous events MUST carry the `nonSourceabilityId` correlation identifier and MUST be validated on receipt; malformed or unresolvable identifiers MUST be discarded with an error log rather than causing silent failures.
- **SOR-003**: Access to `POST /metadata/nonSourceable` and `POST /nonSourceable/{nonSourceabilityId}` MUST enforce authentication and role-based access control consistent with existing Dataland API security policies.

### Key Entities

- **NonSourceabilityInformation** (owned by dataland-backend): Represents the authoritative record that a dataset has been flagged as non-sourceable. Key attributes: `nonSourceabilityId`, `companyId`, `dataType`, `reportingPeriod`, `reason`, `bypassQa`, `qaStatus` (Pending / Accepted / Rejected), `currentlyActive`, `uploadTime`, `uploaderUserId`.
- **NonSourceableQaReviewInformation** (owned by dataland-qa-service): Represents the QA review task for a non-sourceability claim. Key attributes: `nonSourceabilityId` (foreign reference to backend record), `companyId`, `dataType`, `reportingPeriod`, `qaStatus`, `reason`, `uploaderUserId`, `uploadTime`, `reviewerUserId`, `qaComment`.
- **DataSourcingObject** (owned by dataland-data-sourcing-service): Tracks the sourcing lifecycle of a dataset. Its state machine gains two new states: `NonSourceableVerification` (pending QA outcome) and `NonSourceable` (confirmed non-sourceable).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A non-sourceability request submitted with `bypassQa = false` is reflected in both the QA service and the data-sourcing service within 60 seconds of backend event emission, without manual intervention.
- **SC-002**: A QA acceptance decision is reflected in both the backend and the data-sourcing service within 60 seconds of the QA-accepted event emission.
- **SC-003**: A QA rejection decision keeps the dataset in `NonSourceableVerification` and makes it visible for manual QA-team handling without causing inconsistent state across services.
- **SC-004**: Duplicate or replayed non-sourceability events produce no duplicate records or inconsistent states across any of the three services.
- **SC-005**: All active non-sourceability endpoints and processing flows use `NonSourceabilityInformation` as the canonical model in dataland-backend, while legacy `SourceabilityEntity` is retained strictly as backup data and not used as the runtime source of truth.

## Assumptions

- The `bypassQa` flag is intended for administrative or automated use cases and requires an elevated privilege; standard users always go through QA review.
- Each `NonSourceabilityInformation` record is scoped to a `(companyId, dataType, reportingPeriod)` tuple; the system allows only one active entry per tuple at a time.
- The messaging infrastructure (e.g., RabbitMQ) guarantees at-least-once delivery; consumers are responsible for idempotent processing.
- The QA comment field in `NonSourceableQaReviewInformation` is optional; a rejection without a comment is still a valid decision.
- The existing Keycloak-based authentication and role model is reused; no new identity provider or auth mechanism is introduced by this feature.
- The data-sourcing service state machine already supports custom states; `NonSourceableVerification` and `NonSourceable` are new states added to it without removing existing states.
- `SourceabilityEntity` is retained in backend persistence for backup/reference purposes only and is not used as the runtime source of truth for non-sourceability endpoints or event-driven process decisions.
