# Feature Specification: Deactivate Non-Sourceability via Endpoint

**Feature Branch**: `010-nonsource-deactivate`  
**Created**: 2026-04-17  
**Status**: Draft  
**Input**: User description: "Extend non-sourceability endpoint to allow marking a triple as sourceable again by adding currentlyActive field"

## Background

Non-sourceability is assigned to triples of the form `{companyId, dataType, reportingPeriod}`. When an active non-sourceability entry exists (`currentlyActive = true`, `qaStatus = Accepted`), the triple is treated as non-sourceable by the data sourcing pipeline — no dataset upload attempts will be made for it.

Currently, once a triple is marked as non-sourceable there is no supported way to reverse this via the API. This feature extends the existing `/metadata/nonSourceable` endpoint with a `currentlyActive` field so that an admin can explicitly mark a triple as sourceable again.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Admin Reverses Non-Sourceability (Priority: P1)

An admin notices that a company triple that was previously marked as non-sourceable now has a valid data source available. The admin wants to mark the triple as sourceable again so the data sourcing pipeline will resume attempts for it.

**Why this priority**: This is the only new capability introduced by this feature. Without it, the entire feature has no unique value.

**Independent Test**: Can be tested end-to-end by first creating an active non-sourceability entry (bypassQa=true, currentlyActive=true), then submitting a reversal request (bypassQa=true, currentlyActive=false), and verifying the triple is no longer reported as non-sourceable.

**Acceptance Scenarios**:

1. **Given** a triple with an active non-sourceability entry (currentlyActive=true, qaStatus=Accepted), **When** an admin submits bypassQa=true / currentlyActive=false for the same triple, **Then** the existing entry is set to currentlyActive=false, a new entry is recorded with currentlyActive=false / bypassQa=true / qaStatus=Accepted, the response is 201, and the triple is no longer reported as non-sourceable.
2. **Given** a triple with an active non-sourceability entry, **When** the reversal is submitted, **Then** no notification is sent to the data sourcing service.
3. **Given** a triple that has no active non-sourceability entry (already sourceable), **When** an admin submits bypassQa=true / currentlyActive=false, **Then** the response is 409 with a message stating that the triple is already sourceable.
4. **Given** a triple with a pending non-sourceability entry (qaStatus=Pending), **When** an admin submits bypassQa=true / currentlyActive=false, **Then** the response is 409 Conflict (pending entry must be resolved first).

---

### User Story 2 - Admin Marks Triple as Non-Sourceable (Bypass QA) (Priority: P2)

An admin wants to immediately mark a triple as non-sourceable without waiting for QA review, using the existing bypass-QA path. The constraint logic is tightened to prevent double-marking.

**Why this priority**: Existing capability whose constraint changes slightly; admins must be prevented from inadvertently creating duplicate active entries.

**Independent Test**: Can be tested by submitting bypassQa=true / currentlyActive=true for a triple and verifying it becomes immediately non-sourceable. Can also verify the duplicate and pending-entry rejection cases.

**Acceptance Scenarios**:

1. **Given** a triple with no existing non-sourceability entry, **When** an admin submits bypassQa=true / currentlyActive=true, **Then** the triple is immediately marked as non-sourceable (qaStatus=Accepted, currentlyActive=true) and returns 201.
2. **Given** a triple that is already actively non-sourceable, **When** an admin submits bypassQa=true / currentlyActive=true, **Then** the response is 409 Conflict because the triple is already non-sourceable.
3. **Given** a triple with a pending non-sourceability entry, **When** an admin submits bypassQa=true / currentlyActive=true, **Then** the response is 409 Conflict because a pending entry must be resolved first.

---

### User Story 3 - Standard User Submits Non-Sourceability for QA Review (Priority: P3)

An uploader identifies a triple for which no valid data source exists and submits a non-sourceability request. The request enters a QA review queue; until approved the triple remains sourceable.

**Why this priority**: This is the existing dominant use case. The behaviour is unchanged except for tightened constraints to prevent uploads when the triple is already actively non-sourceable or already pending.

**Independent Test**: Can be tested by submitting bypassQa=false / currentlyActive=false and verifying a pending entry is created. Rejection cases (active entry exists, pending entry exists) can be tested independently.

**Acceptance Scenarios**:

1. **Given** a triple with no existing non-sourceability entry, **When** a user submits bypassQa=false / currentlyActive=false, **Then** an entry with qaStatus=Pending and currentlyActive=false is created and returns 201.
2. **Given** a triple with an active non-sourceability entry (currentlyActive=true), **When** a user submits bypassQa=false / currentlyActive=false, **Then** the response is 409 Conflict (triple is already non-sourceable).
3. **Given** a triple with a pending non-sourceability entry, **When** a user submits bypassQa=false / currentlyActive=false, **Then** the response is 409 Conflict (must wait for the pending entry to be resolved first).

---

### User Story 4 - Invalid Combination Rejection (Priority: P4)

Any caller submits bypassQa=false together with currentlyActive=true, which is a logically inconsistent combination (setting a triple as actively non-sourceable without QA approval is not permitted for non-admin submissions, and the QA service—not the caller—sets currentlyActive=true).

**Why this priority**: Defensive validation to prevent misuse; lower priority than the primary flows.

**Independent Test**: Submit bypassQa=false / currentlyActive=true and verify a clear error response is returned.

**Acceptance Scenarios**:

1. **Given** any triple, **When** any caller submits bypassQa=false / currentlyActive=true, **Then** the request is rejected with a clear error message explaining that currentlyActive=true requires bypassQa=true.

---

### Edge Cases

- What happens when a triple has multiple historical entries (some with currentlyActive=false) but none currently active, and the admin tries to deactivate? → Returns 409 (already sourceable).
- What happens when a pending entry exists and the reversal (bypassQa=true, currentlyActive=false) is attempted? → Rejected; pending entry must be resolved first.
- What happens when an admin sends bypassQa=true / currentlyActive=false for a triple that was never marked non-sourceable? → Returns 409 with message that the triple is already sourceable.
- What happens when a non-admin submits bypassQa=true? → Rejected with an authorisation error (existing behaviour, unchanged).

## Clarifications

### Session 2026-04-17

- Q: When `bypassQa=true` but `currentlyActive` is omitted, should the field default or be required? → A: Required — must be explicitly provided. Follows the existing `@field:JsonProperty(required = true)` pattern used for all mandatory fields in `NonSourceabilityRequest.kt`.
- Q: What HTTP status code should all constraint-violation rejections (duplicate active entry, pending entry exists, etc.) return? → A: 409 Conflict — consistent with the existing uniqueness-constraint rejection pattern in the backend.
- Q: Does the QA service set `currentlyActive=true` when approving a standard non-sourceability entry? → A: Yes, but that happens later (entry starts as Pending / currentlyActive=false); the QA approval path is existing behaviour and is out of scope for this implementation.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The `/metadata/nonSourceable` endpoint MUST accept a **required** `currentlyActive` boolean field in the request body. Omitting the field MUST result in a 400 Bad Request. This follows the existing `@field:JsonProperty(required = true)` convention used for all mandatory fields in the request model.
- **FR-002**: The combination `bypassQa=false, currentlyActive=true` MUST be rejected with a descriptive error message stating that active non-sourceability without QA bypass is not permitted.
- **FR-003**: For `bypassQa=false, currentlyActive=false`: the request MUST be rejected with 409 Conflict if an entry with `currentlyActive=true` already exists for the triple.
- **FR-004**: For `bypassQa=false, currentlyActive=false`: the request MUST be rejected with 409 Conflict if an entry with `qaStatus=Pending` already exists for the triple.
- **FR-005**: For `bypassQa=true, currentlyActive=true`: the request MUST be rejected with 409 Conflict if an entry with `currentlyActive=true` already exists for the triple.
- **FR-006**: For `bypassQa=true, currentlyActive=true`: the request MUST be rejected with 409 Conflict if an entry with `qaStatus=Pending` already exists for the triple.
- **FR-007**: For `bypassQa=true, currentlyActive=false` when an active entry (`currentlyActive=true`) exists: the system MUST set that existing entry's `currentlyActive` to `false` (preserving its `qaStatus=Accepted`), create a new entry with `currentlyActive=false / bypassQa=true / qaStatus=Accepted`, and return 201.
- **FR-008**: For `bypassQa=true, currentlyActive=false` when no active entry exists: the system MUST return 409 with a message stating the triple is already sourceable.
- **FR-009**: For `bypassQa=true, currentlyActive=false` when a pending entry exists: the request MUST be rejected with 409 Conflict.
- **FR-010**: The `bypassQa=true, currentlyActive=false` deactivation flow MUST NOT trigger any notification to the data sourcing service.
- **FR-011**: All `bypassQa=true` operations MUST remain restricted to admin users only (existing authorisation requirement, unchanged).
- **FR-012**: After a successful deactivation (FR-007), the triple MUST no longer be reported as non-sourceable by the non-sourceability query endpoint.

### Key Entities

- **NonSourceabilityInformation**: Represents a non-sourceability record for a `{companyId, dataType, reportingPeriod}` triple. Key fields: `nonSourceabilityId`, `companyId`, `dataType`, `reportingPeriod`, `qaStatus` (Pending / Accepted / Rejected), `currentlyActive` (boolean), `bypassQa` (boolean), `reason` (free text), `uploaderUserId`, `uploadTime`.
- **Triple**: The composite key `{companyId, dataType, reportingPeriod}` that uniquely identifies a non-sourceability scope.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An admin can reverse a non-sourceability marking for a triple in a single API call, with the triple immediately becoming sourceable upon the response.
- **SC-002**: The audit trail is fully preserved — no entries are deleted; all state transitions are recorded as new entries or field updates, making the history reconstructable by inspection order.
- **SC-003**: All four `bypassQa × currentlyActive` combinations return the correct HTTP status code and a meaningful error message on rejection in 100% of cases.
- **SC-004**: The data sourcing service receives zero spurious notifications as a result of the deactivation flow.
- **SC-005**: All existing non-sourceability workflows (standard QA submission, admin bypass-mark) continue to function correctly after the change.

## Assumptions

- The `currentlyActive` field is required in all requests (no default). Existing callers that omit it will need to be updated to pass `currentlyActive: false` explicitly.
- Only one entry per triple can have `currentlyActive=true` at any point in time (enforced by existing business logic and the new constraints).
- The QA service is the only actor that sets `currentlyActive=true` for QA-reviewed (`bypassQa=false`) entries; it does so upon approval (after an interim Pending / currentlyActive=false state). This existing behaviour is unchanged and out of scope for this implementation.
- "No notification to the data sourcing service" means no message is published to the RabbitMQ exchange that would trigger data sourcing pipeline resumption — the deactivation is intentional and should not cause an immediate sourcing attempt.
- The reason field remains free-text and optional; this feature does not constrain its format.
- Frontend changes (UI for the new field) are out of scope for this specification; the API change is backend-only.
