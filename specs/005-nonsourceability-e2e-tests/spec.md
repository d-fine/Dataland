# Feature Specification: NonSourceability QA Lifecycle E2E Test

**Feature Branch**: `005-nonsourceability-e2e-tests`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Make E2E tests for NonSourceability feature: POST to backend (bypassQa false), check in backend, check in QA controller, accept in QA controller, verify QA status updated and currentlyActive is true in backend"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Full QA Lifecycle for Non-Sourceability (Priority: P1)

An admin submits a non-sourceability request for a company/datatype/reportingPeriod triple via the backend without bypassing the QA review step. The system records the entry as pending and inactive, propagates it to the QA service, and — after a QA reviewer accepts it — becomes active in the backend.

**Why this priority**: This is the central happy-path of the new unified non-sourceability flow. It exercises the entire cross-service lifecycle: backend entry creation, event-driven QA row creation, QA review, event-driven backend state update.

**Independent Test**: Can be fully tested by running a single Kotlin JUnit test against a live stack: create a company, POST non-sourceability with `bypassQa=false`, then assert state in backend and QA service at each transition.

**Acceptance Scenarios**:

1. **Given** a company exists in the backend,  
   **When** an admin POSTs to `POST /metadata/nonSourceable` with `bypassQa=false`,  
   **Then** the response contains a `NonSourceabilityInformationResponse` with `qaStatus=Pending` and `currentlyActive=false`.

2. **Given** the non-sourceability entry was just created,  
   **When** the admin GETs `GET /metadata/nonSourceable` filtered by the same `(companyId, dataType, reportingPeriod)`,  
   **Then** exactly one entry is returned with `qaStatus=Pending` and `currentlyActive=false`.

3. **Given** the non-sourceability entry was created in the backend,  
   **When** the admin GETs `GET /nonSourceable` (or `GET /nonSourceable/queue`) in the QA service filtered by `(companyId, dataType, reportingPeriod)`,  
   **Then** a matching `NonSourceableQaReviewInformation` row exists with `qaStatus=Pending` (the event-driven creation may require a short polling wait).

4. **Given** the QA review row exists in the QA service with `qaStatus=Pending`,  
   **When** a QA reviewer POSTs `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Accepted`,  
   **Then** the response is `200`, and the QA row's `qaStatus` is now `Accepted`.

5. **Given** the QA decision was accepted,  
   **When** the admin GETs `GET /metadata/nonSourceable` in the backend for the same triple,  
   **Then** the entry has `qaStatus=Accepted` and `currentlyActive=true` (the event-driven update may require a short polling wait).

---

### Edge Cases

- If an entry already exists with `qaStatus=Pending` or `Accepted` for the same `(companyId, dataType, reportingPeriod)`, a second POST is rejected with a `400` error.
- If a non-admin attempts to set `bypassQa=true`, the request is rejected with a `403` error.
- The QA row in the QA service appears asynchronously after the backend POST; assertions must tolerate brief propagation delays.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The test MUST register a new company in the backend before posting non-sourceability so it tests a clean, isolated state.
- **FR-002**: The test MUST POST to `POST /metadata/nonSourceable` as an admin user with `bypassQa=false` and assert the returned `NonSourceabilityInformationResponse` contains `qaStatus=Pending` and `currentlyActive=false`.
- **FR-003**: The test MUST retrieve the entry via `GET /metadata/nonSourceable` in the backend and assert it matches the posted triple with `qaStatus=Pending` and `currentlyActive=false`.
- **FR-004**: The test MUST retrieve the entry via `GET /nonSourceable` (or `GET /nonSourceable/queue`) in the QA service and assert a matching review row exists with `qaStatus=Pending`, using retry logic to account for asynchronous event propagation.
- **FR-005**: The test MUST POST an `Accepted` decision to `POST /nonSourceable/{nonSourceabilityId}` in the QA service as a QA reviewer (or admin), and assert the response indicates success.
- **FR-006**: The test MUST assert the `NonSourceableQaReviewInformation` returned directly in the POST response from `POST /nonSourceable/{nonSourceabilityId}` shows `qaStatus=Accepted` (no separate GET re-fetch is required, as the POST response body reflects the updated state).
- **FR-007**: The test MUST re-fetch the backend entry via `GET /metadata/nonSourceable` and assert `qaStatus=Accepted` and `currentlyActive=true`, using retry logic to account for asynchronous event propagation.
- **FR-008**: Each step MUST use the appropriate technical user role (admin for backend writes, admin or reviewer for QA decisions) to match the real access-control rules.

### Key Entities

- **NonSourceabilityInformationResponse** (backend): Represents a non-sourceability record. Key fields: `nonSourceabilityId`, `companyId`, `dataType`, `reportingPeriod`, `qaStatus`, `currentlyActive`, `uploaderUserId`, `uploadTime`.
- **NonSourceabilityRequest** (backend POST body): `companyId`, `dataType`, `reportingPeriod`, `reason`, `bypassQa`.
- **NonSourceableQaReviewInformation** (QA service): Mirrors the backend entry for QA review purposes. Key fields: `nonSourceabilityId`, `companyId`, `dataType`, `reportingPeriod`, `qaStatus`, `reason`, `uploaderUserId`, `uploadTime`, `reviewerUserId`, `qaComment`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The single E2E test runs to completion without assertion failures against a live stack.
- **SC-002**: All five acceptance scenarios (POST response, GET backend pending, GET QA pending, POST QA accept and verify returned row, GET backend accepted+active) pass within the test's allowed retry window (default `awaitUntilAsserted` timeout).
- **SC-003**: The test is idempotent: re-running it on a fresh company/period combination always passes, with no leftover state interfering.

## Assumptions

- A running local Dataland stack (`manageLocalStack.sh --start --simple`) is a prerequisite; the test does not start any services.
- `GlobalAuth.withTechnicalUser(TechnicalUser.Admin)` provides a valid admin token for both the backend and the QA service calls.
- `awaitUntilAsserted` (existing test utility) is used for all assertions that depend on asynchronous event propagation (QA row creation, backend `currentlyActive` update).
- The `dataType` used in the test is `sfdr` (consistent with the existing `NonSourceabilityTest` skeleton), but any registered data type is acceptable.
- No pre-existing dataset for the chosen triple is needed; the non-sourceability endpoint accepts the entry even without an existing dataset.
- The `nonSourceabilityId` needed for the QA POST is retrieved from the response of the initial backend POST.
