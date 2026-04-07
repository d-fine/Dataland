# RabbitMQ Event Schemas

Async event messages published by the three Dataland microservices to coordinate non-sourceability workflow.

## Overview

Four event types are defined:

1. **non-sourceability-created** – Backend publishes when QA review is needed (bypassQa=false)
2. **non-sourceability-auto-accepted** – Backend publishes when request bypasses QA (bypassQa=true)
3. **QA-accepted** – QA Service publishes when reviewer approves the claim
4. **QA-rejected** – QA Service publishes when reviewer rejects the claim

All events include `nonSourceabilityId` as the correlation ID for idempotent replay and traceability.

---

## Event: NonSourceabilityCreatedEvent

**Published by**: dataland-backend  
**Consumers**: dataland-qa-service, dataland-data-sourcing-service  
**Exchange**: `dataland-events` (type: topic)  
**Routing Key**: `non-sourceability.created`  
**Queue Bindings**:
- QA Service: `non-sourceability-created-queue` → subscribes to `dataland-events` with routing key `non-sourceability.created`
- Data-Sourcing Service: `non-sourceability-created-queue` → subscribes to `dataland-events` with routing key `non-sourceability.created`

### Payload Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "NonSourceabilityCreatedEvent",
  "type": "object",
  "required": [
    "eventId",
    "nonSourceabilityId",
    "companyId",
    "dataType",
    "reportingPeriod",
    "reason",
    "uploaderUserId",
    "uploadTime",
    "eventPublishedTime"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid",
      "description": "Unique event instance ID (different from nonSourceabilityId)"
    },
    "nonSourceabilityId": {
      "type": "string",
      "format": "uuid",
      "description": "Correlation ID linking all related events for this non-sourceability claim"
    },
    "companyId": {
      "type": "string",
      "format": "uuid"
    },
    "dataType": {
      "type": "string",
      "description": "Type of data (e.g., sustainability-data)"
    },
    "reportingPeriod": {
      "type": "string",
      "description": "Reporting period (e.g., 2024-12-31)"
    },
    "reason": {
      "type": "string",
      "description": "Justification for non-sourceability"
    },
    "uploaderUserId": {
      "type": "string",
      "description": "User who submitted the claim"
    },
    "uploadTime": {
      "type": "string",
      "format": "date-time",
      "description": "Timestamp of claim creation (UTC)"
    },
    "eventPublishedTime": {
      "type": "string",
      "format": "date-time",
      "description": "Timestamp when event was published (UTC)"
    }
  }
}
```

### Consumer Action

- **QA Service**: Create NonSourceableQaReviewInformation with nonSourceabilityId, qaStatus=Pending
- **Data-Sourcing Service**: Transition dataset state to NonSourceableVerification

---

## Event: NonSourceabilityAutoAcceptedEvent

**Published by**: dataland-backend  
**Consumers**: dataland-data-sourcing-service  
**Exchange**: `dataland-events` (type: topic)  
**Routing Key**: `non-sourceability.auto-accepted`  
**Queue Bindings**:
- Data-Sourcing Service: `non-sourceability-auto-accepted-queue` → subscribes to `dataland-events` with routing key `non-sourceability.auto-accepted`

### Payload Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "NonSourceabilityAutoAcceptedEvent",
  "type": "object",
  "required": [
    "eventId",
    "nonSourceabilityId",
    "companyId",
    "dataType",
    "reportingPeriod",
    "reason",
    "uploaderUserId",
    "uploadTime",
    "eventPublishedTime"
  ],
  "properties": {
    "eventId": { "type": "string", "format": "uuid" },
    "nonSourceabilityId": { "type": "string", "format": "uuid" },
    "companyId": { "type": "string", "format": "uuid" },
    "dataType": { "type": "string" },
    "reportingPeriod": { "type": "string" },
    "reason": { "type": "string" },
    "uploaderUserId": { "type": "string" },
    "uploadTime": { "type": "string", "format": "date-time" },
    "eventPublishedTime": { "type": "string", "format": "date-time" }
  }
}
```

### Consumer Action

- **Data-Sourcing Service**: Transition dataset state directly to NonSourceable (skipping verification step)

---

## Event: QaNonSourceabilityAcceptedEvent

**Published by**: dataland-qa-service  
**Consumers**: dataland-backend, dataland-data-sourcing-service  
**Exchange**: `dataland-events` (type: topic)  
**Routing Key**: `qa.decision.accepted`  
**Queue Bindings**:
- Backend: `qa-decision-queue` → subscribes to `dataland-events` with routing key `qa.decision.*`
- Data-Sourcing Service: `qa-decision-queue` → subscribes to `dataland-events` with routing key `qa.decision.*`

### Payload Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "QaNonSourceabilityAcceptedEvent",
  "type": "object",
  "required": [
    "eventId",
    "nonSourceabilityId",
    "qaStatus",
    "reviewerUserId",
    "decisionTime",
    "eventPublishedTime"
  ],
  "properties": {
    "eventId": {
      "type": "string",
      "format": "uuid",
      "description": "Unique event ID"
    },
    "nonSourceabilityId": {
      "type": "string",
      "format": "uuid",
      "description": "Correlation ID"
    },
    "qaStatus": {
      "type": "string",
      "enum": ["ACCEPTED"],
      "description": "Always Accepted for this event type"
    },
    "reviewerUserId": {
      "type": "string",
      "description": "QA reviewer user ID"
    },
    "qaComment": {
      "type": "string",
      "nullable": true,
      "description": "Optional reviewer comment"
    },
    "decisionTime": {
      "type": "string",
      "format": "date-time",
      "description": "When decision was recorded (UTC)"
    },
    "eventPublishedTime": {
      "type": "string",
      "format": "date-time",
      "description": "When event was published (UTC)"
    }
  }
}
```

### Consumer Action

- **Backend**: Update NonSourceabilityInformation: set qaStatus=Accepted, currentlyActive=true
- **Data-Sourcing Service**: Transition dataset state from NonSourceableVerification to NonSourceable

---

## Event: QaNonSourceabilityRejectedEvent

**Published by**: dataland-qa-service  
**Consumers**: dataland-backend, dataland-data-sourcing-service  
**Exchange**: `dataland-events` (type: topic)  
**Routing Key**: `qa.decision.rejected`  
**Queue Bindings**: Same as QA-Accepted (single queue receives both decision types via `qa.decision.*`)

### Payload Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "QaNonSourceabilityRejectedEvent",
  "type": "object",
  "required": [
    "eventId",
    "nonSourceabilityId",
    "qaStatus",
    "reviewerUserId",
    "decisionTime",
    "eventPublishedTime"
  ],
  "properties": {
    "eventId": { "type": "string", "format": "uuid" },
    "nonSourceabilityId": { "type": "string", "format": "uuid" },
    "qaStatus": {
      "type": "string",
      "enum": ["REJECTED"],
      "description": "Always Rejected for this event type"
    },
    "reviewerUserId": { "type": "string" },
    "qaComment": { "type": "string", "nullable": true },
    "decisionTime": { "type": "string", "format": "date-time" },
    "eventPublishedTime": { "type": "string", "format": "date-time" }
  }
}
```

### Consumer Action

- **Backend**: Update NonSourceabilityInformation: set qaStatus=REJECTED (leave currentlyActive=false)
- **Data-Sourcing Service**: Leave dataset state in NonSourceableVerification (manual QA-Team handling required)

---

## Idempotency Guarantees

All consumers MUST implement idempotent handlers:

1. **Check by nonSourceabilityId**: If a record already exists with the target state, skip the update
2. **Use event time as tiebreaker**: If two events arrive out of order, use eventPublishedTime to determine causality
3. **Log duplicate replays**: If a duplicate event is processed, log it for monitoring

Example (Kotlin):

```kotlin
fun handleQaAcceptedEvent(event: QaNonSourceabilityAcceptedEvent) {
    val existing = repository.findByNonSourceabilityId(event.nonSourceabilityId)
    if (existing?.qaStatus == QaStatus.ACCEPTED && existing.updatedTime >= event.decisionTime) {
        logger.warn("Duplicate QA-Accepted event received: $event")
        return  // Idempotent: already processed
    }
    // Proceed with update
    existing.qaStatus = QaStatus.ACCEPTED
    existing.currentlyActive = true
    repository.save(existing)
}
```

---

## Backward Compatibility

- All events are versioned by eventId and should include a schema version if future changes are needed
- New fields MUST be additive and optional (no breaking changes)
- Deprecated fields MUST be retained until all consumers have migrated
- Event type additions (e.g., new QA decision types) do not affect existing consumers

---

## RabbitMQ Topology Reference

```
Exchange: dataland-events (type: topic, durable: true)

Queues:
- non-sourceability-created-queue     (durable: true, auto-delete: false)
  Binding: dataland-events, routing key: "non-sourceability.created"
  Consumers: dataland-qa-service, dataland-data-sourcing-service

- non-sourceability-auto-accepted-queue (durable: true)
  Binding: dataland-events, routing key: "non-sourceability.auto-accepted"
  Consumers: dataland-data-sourcing-service

- qa-decision-queue                   (durable: true)
  Binding: dataland-events, routing key: "qa.decision.*"
  Consumers: dataland-backend, dataland-data-sourcing-service

Dead Letter Exchange: dataland-events-dlx (for failed message replay)
  Dead Letter Queue: dataland-events-dlq
  TTL: 15 minutes (configurable)
```

---

## Changelog

### Version 1.0.0 (2026-04-07)

- Initial event schema definitions
- Four event types: NonSourceabilityCreatedEvent, NonSourceabilityAutoAcceptedEvent, QaNonSourceabilityAcceptedEvent, QaNonSourceabilityRejectedEvent
- Idempotency requirement documented
