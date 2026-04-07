# Data Model: Unified Non-Sourceability Lifecycle

**Created**: 2026-04-07  
**Feature**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## Overview

Three new domain entities introduced across Dataland microservices to support the unified non-sourceability evaluation workflow.

## Entity 1: NonSourceabilityInformation (dataland-backend)

**Purpose**: Authoritative record of a non-sourceability claim for a dataset.

**Ownership**: `dataland-backend`

**Storage**: PostgreSQL table `non_sourceability_information`

### Attributes

| Attribute | Type | Nullable | Constraint | Notes |
|-----------|------|----------|-----------|-------|
| `nonSourceabilityId` | UUID | NO | PRIMARY KEY | Unique identifier; used as correlation ID in all events |
| `companyId` | UUID | NO | FOREIGN KEY | Links to dataset owner company |
| `dataType` | String | NO | PART OF UNIQUE KEY | Type of data (e.g., "sustainability-data") |
| `reportingPeriod` | String | NO | PART OF UNIQUE KEY | Reporting period (e.g., "2024-12-31") |
| `reason` | String | NO | | Justification for non-sourceability claim |
| `uploaderUserId` | String | NO | | Username/ID of user who created the claim |
| `uploadTime` | ZonedDateTime | NO | | Timestamp when claim was recorded (UTC) |
| `qaStatus` | Enum | NO | DEFAULT: PENDING | {PENDING, ACCEPTED, REJECTED} |
| `currentlyActive` | Boolean | NO | DEFAULT: false | Indicates if claim is actively enforced |
| `bypassQa` | Boolean | NO | DEFAULT: false | Audit trail: whether QA was bypassed |

### Unique Constraints

- **UNIQUE(companyId, dataType, reportingPeriod, qaStatus)** where qaStatus ∈ {PENDING, ACCEPTED}
  - Prevents duplicate active requests for the same dataset
  - Allows multiple historical records with status REJECTED

### State Transitions

```
  [CREATE with bypassQa=false]
       ↓
  qaStatus=PENDING, currentlyActive=false
       ↓
  [QA-Accepted event received]
       ↓
  qaStatus=ACCEPTED, currentlyActive=true
       ↓
  [Data upload approved by admin]
       ↓
  currentlyActive=false (superseded by new upload)

  OR

  [CREATE with bypassQa=true]
       ↓
  qaStatus=ACCEPTED, currentlyActive=true
       ↓
  [Data upload approved by admin]
       ↓
  currentlyActive=false

  OR

  [CREATE with bypassQa=false]
       ↓
  qaStatus=PENDING, currentlyActive=false
       ↓
  [QA-Rejected event received]
       ↓
  qaStatus=REJECTED, currentlyActive=false
```

---

## Entity 2: NonSourceableQaReviewInformation (dataland-qa-service)

**Purpose**: QA review task tracking for non-sourceability claims.

**Ownership**: `dataland-qa-service`

**Storage**: PostgreSQL table `non_sourceable_qa_review_information`

### Attributes

| Attribute | Type | Nullable | Constraint | Notes |
|-----------|------|----------|-----------|-------|
| `id` | UUID | NO | PRIMARY KEY | Local primary key for the QA review record |
| `nonSourceabilityId` | UUID | NO | FOREIGN KEY | Correlation ID linking to backend's NonSourceabilityInformation (federation key, not local FK) |
| `companyId` | UUID | NO | | Denormalized company reference for query optimization |
| `dataType` | String | NO | | Denormalized data type for query optimization |
| `reportingPeriod` | String | NO | | Denormalized period for query optimization |
| `reason` | String | NO | | Copy of reason from backend for context |
| `uploaderUserId` | String | NO | | Copy of original uploader for audit trail |
| `uploadTime` | ZonedDateTime | NO | | Copied from backend |
| `qaStatus` | Enum | NO | DEFAULT: PENDING | {PENDING, ACCEPTED, REJECTED} |
| `reviewerUserId` | String | YES | | Username/ID of QA reviewer (set when status != PENDING) |
| `qaComment` | String | YES | | Optional comment from QA reviewer |
| `reviewTimestamp` | ZonedDateTime | YES | | Timestamp when decision was recorded |

### Indexes

- **INDEX (nonSourceabilityId)** – Used for event-driven lookups
- **INDEX (qaStatus, uploadTime DESC)** – Used for queue view (pending tasks sorted by date)
- **INDEX (companyId, dataType, reportingPeriod, qaStatus)** – Used for detailed queries

### State Transitions

```
  [Non-Sourceability-Created event received]
       ↓
  qaStatus=PENDING, reviewerUserId=null, qaComment=null
       ↓
  [Reviewer submits decision]
       ↓
  qaStatus=ACCEPTED/REJECTED, reviewerUserId=<id>, qaComment=<optional>
       ↓
  [Immutable; decision is recorded]
```

---

## Entity 3: DataSourcingObject State Extension (dataland-data-sourcing-service)

**Purpose**: Track dataset sourcing lifecycle including non-sourceability states.

**Ownership**: `dataland-data-sourcing-service`

**Storage**: Existing PostgreSQL table `data_sourcing_object` (column: `state`)

### State Enum Additions

Existing DataSourcingState enum gains two new values:

```kotlin
enum class DataSourcingState {
    // Existing states
    DATA_SOURCING,
    DATA_SOURCING_DONE,
    DOCUMENT_SOURCING,
    DOCUMENT_SOURCING_DONE,
    
    // NEW: Non-sourceability lifecycle
    NON_SOURCEABLE_VERIFICATION,  // Awaiting QA decision on non-sourceability claim
    NON_SOURCEABLE                // Confirmed non-sourceable; no updates expected
}
```

### State Machine Diagram

```
Existing workflow:
  DATA_SOURCING → DATA_SOURCING_DONE → DOCUMENT_SOURCING → DOCUMENT_SOURCING_DONE

NEW: Non-sourceability branch (can occur at any point):

  DATA_SOURCING
    ↓
  [Non-Sourceability-Created event, bypassQa=false]
    ↓
  NON_SOURCEABLE_VERIFICATION
    ↓
  [QA-Accepted event]
    ↓
  NON_SOURCEABLE (final; no further updates)
    
  OR
  
  [QA-Rejected event]
    ↓
  NON_SOURCEABLE_VERIFICATION (remains; manual QA-Team handling)

Shortcut (bypassQa=true):

  DATA_SOURCING
    ↓
  [Non-Sourceability-Auto-Accepted event]
    ↓
  NON_SOURCEABLE (direct; no QA step)
```

### Authorization for State Transitions

**New Rule**: Only admins can explicitly patch to NON_SOURCEABLE or other sensitive states.

- Update `canUserPatchState()` in `DataSourcingStateSecurityService` to enforce role-based checks
- Document in OpenAPI: `PATCH /data-sourcing/{dataSourcingId}/state`
- Exception: Event-driven state transitions (from message handlers) bypass this check; events are trusted from internal message bus

---

## Relationships & Data Flow

### Cross-Service References

```
nonSourceabilityId (UUID)
    ↓
    ├─→ Backend: NonSourceabilityInformation.nonSourceabilityId (owns)
    ├─→ QA Service: NonSourceableQaReviewInformation.nonSourceabilityId (federates)
    └─→ Data-Sourcing: [implicit via event payload; no direct FK]
```

**Key Insight**: `nonSourceabilityId` is the sole correlation identifier across all three services. All events carry this ID to ensure idempotent processing and consistent state.

### Event Payload Structure

All events include:
```json
{
  "eventId": "UUID (unique per event instance)",
  "nonSourceabilityId": "UUID (shared across all related events)",
  "companyId": "UUID",
  "dataType": "String",
  "reportingPeriod": "String",
  "eventPublishedTime": "ZonedDateTime (UTC)"
}
```

Additional fields vary by event type (see contracts/).

---

## Backward Compatibility

### Existing Entities

- **No breaking changes** to existing SourceabilityEntity (if it exists) – Non-sourceability is a separate workflow
- **HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}** changes logic to query NonSourceabilityInformation instead of SourceabilityEntity
- Existing API clients must be updated to handle the new 200 response codes, but no breaking schema changes

### RabbitMQ Topology

- Four new event types introduced (non-sourceability-created, non-sourceability-auto-accepted, QA-accepted, QA-rejected)
- No changes to existing event types or queue bindings
- New queues: `non-sourceability-created-queue`, `qa-decision-queue` (backend & data-sourcing services both subscribe)

---

## Validation Rules

### At Create Time (Backend)

1. **Duplicate Check**: If a record exists with same (companyId, dataType, reportingPeriod) AND qaStatus ∈ {PENDING, ACCEPTED}, reject with 409 Conflict
2. **Authorization Check**: If bypassQa=true and user is not admin, reject with 403 Forbidden
3. **Audit Logging**: If bypassQa=true, log the request (user ID, timestamp, dataset identifiers, reason, comment if provided)

### At QA Decision Time

1. **Idempotent Decision Storage**: If the same nonSourceabilityId is received twice with the same qaStatus, return 200 OK without creating duplicate records
2. **No State Regression**: Cannot change qaStatus from ACCEPTED back to PENDING (one-way transition)
3. **Reviewer Identity**: System user ID from JWT/auth context is recorded as reviewerUserId

### Event Handler Idempotency

1. **Backend Event Handler**: When receiving QA-decision event, check if NonSourceabilityInformation already has matching qaStatus; if yes, skip update (idempotent)
2. **QA Service Event Handler**: When receiving non-sourceability-created event, check if NonSourceableQaReviewInformation with that nonSourceabilityId already exists; if yes, skip creation (idempotent)
3. **Data-Sourcing Event Handler**: When receiving state-change event, check if DataSourcingObject is already in target state; if yes, skip update (idempotent)

---

## Performance Considerations

### Indexing Strategy

- **Backend**: UNIQUE(companyId, dataType, reportingPeriod, qaStatus WHERE qaStatus != REJECTED) for duplicate detection
- **QA Service**: INDEX(nonSourceabilityId) for event lookups; INDEX(qaStatus, uploadTime DESC) for queue view
- **Data-Sourcing**: Existing state indexes sufficient; no new multi-column indexes needed

### Query Patterns

- **Backend GET /metadata/nonSourceable**: Filter by companyId, dataType, reportingPeriod, qaStatus → well-indexed
- **QA Service GET /nonSourceable/queue**: Filter by qaStatus=PENDING, order by uploadTime → well-indexed
- **Data-Sourcing state lookup**: Existing patterns; no new complexity

### Eventual Consistency

- All three services are eventually consistent via RabbitMQ messaging
- Backend is the source of truth for `qaStatus` and `currentlyActive`
- QA Service and Data-Sourcing Service derive state from events
- Maximum delay: 60 seconds (per SC-001 success criterion)

---

## Testing Data Structures

### Test Fixtures

```kotlin
// Backend test fixture
val testNonSourceability = NonSourceabilityInformation(
    nonSourceabilityId = UUID.randomUUID(),
    companyId = UUID.randomUUID(),
    dataType = "sustainability-data",
    reportingPeriod = "2024-12-31",
    reason = "Data quality issues",
    uploaderUserId = "test-user-1",
    uploadTime = ZonedDateTime.now(ZoneId.of("UTC")),
    qaStatus = NonSourceabilityQaStatus.PENDING,
    currentlyActive = false
)

// QA Service test fixture
val testQaReview = NonSourceableQaReviewInformation(
    nonSourceabilityId = testNonSourceability.nonSourceabilityId,
    companyId = testNonSourceability.companyId,
    dataType = testNonSourceability.dataType,
    reportingPeriod = testNonSourceability.reportingPeriod,
    reason = testNonSourceability.reason,
    uploaderUserId = testNonSourceability.uploaderUserId,
    uploadTime = testNonSourceability.uploadTime,
    qaStatus = NonSourceabilityQaStatus.PENDING,
    reviewerUserId = null,
    qaComment = null,
    reviewTimestamp = null
)

// Data-Sourcing test fixture (state only)
val dataSourcingObjectWithNonSourceableState = DataSourcingObject(
    // ... existing fields ...
    state = DataSourcingState.NON_SOURCEABLE_VERIFICATION
)
```

### Test Tables

- **test_non_sourceability_information** (isolated from production data)
- **test_non_sourceable_qa_review_information** (isolated from production data)
- **test_data_sourcing_objects** (with new state values)
- **test_rabbitmq_messages** (for idempotency verification)
