# Non-Sourceability Messaging Contract

## Implementation Status
All four event types are implemented and wired end-to-end.

### Exchange / Queue Summary
| Constant | Value |
|---|---|
| `ExchangeName.BACKEND_DATA_NONSOURCEABLE` | `backend.dataNonSourceable` |
| `ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS` | `qa-service.nonSourceabilityDecisions` |
| `QueueNames.QA_SERVICE_NON_SOURCEABILITY_CREATED` | `qa-service.processNonSourceabilityCreated` |
| `QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_CREATED` | `data-sourcing-service.processNonSourceabilityCreated` |
| `QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_AUTO_ACCEPTED` | `data-sourcing-service.processNonSourceabilityAutoAccepted` |
| `QueueNames.BACKEND_NON_SOURCEABILITY_QA_DECISION` | `backend.processNonSourceabilityQaDecision` |
| `QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_QA_DECISION` | `data-sourcing-service.processNonSourceabilityQaDecision` |

## Exchanges and Event Types

### Event A: non-sourceability-created
- Producer: `dataland-backend` → `NonSourceabilityInformationManager.emitLifecycleEvent`
- Emitted when: `POST /metadata/nonSourceable` with `bypassQa=false`
- Exchange: `BACKEND_DATA_NONSOURCEABLE`
- Routing key: `NON_SOURCEABILITY_CREATED`
- Consumers:
  - `dataland-qa-service` `NonSourceabilityEventListener` → create `NonSourceableQaReviewInformationEntity` (Pending)
  - `dataland-data-sourcing-service` `NonSourceabilityEventListener` → transition to `NonSourceableVerification`

### Event B: non-sourceability-auto-accepted
- Producer: `dataland-backend` → `NonSourceabilityInformationManager.emitLifecycleEvent`
- Emitted when: `POST /metadata/nonSourceable` with `bypassQa=true`
- Exchange: `BACKEND_DATA_NONSOURCEABLE`
- Routing key: `NON_SOURCEABILITY_AUTO_ACCEPTED`
- Consumers:
  - `dataland-data-sourcing-service` `NonSourceabilityEventListener` → transition to `NonSourceable`

### Event C: non-sourceability-qa-accepted
- Producer: `dataland-qa-service` → `NonSourceabilityQaReviewManager.postDecision`
- Emitted when: `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Accepted`
- Exchange: `QA_SERVICE_NON_SOURCEABILITY_DECISIONS`
- Routing key: `NON_SOURCEABILITY_QA_ACCEPTED`
- Consumers:
  - `dataland-backend` `NonSourceabilityQaDecisionListener` → set `qaStatus=Accepted`, `currentlyActive=true`
  - `dataland-data-sourcing-service` `NonSourceabilityQaDecisionListener` → transition to `NonSourceable`

### Event D: non-sourceability-qa-rejected
- Producer: `dataland-qa-service` → `NonSourceabilityQaReviewManager.postDecision`
- Emitted when: `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Rejected`
- Exchange: `QA_SERVICE_NON_SOURCEABILITY_DECISIONS`
- Routing key: `NON_SOURCEABILITY_QA_REJECTED`
- Consumers:
  - `dataland-backend` `NonSourceabilityQaDecisionListener` → set `qaStatus=Rejected`, `currentlyActive=false`
  - `dataland-data-sourcing-service` `NonSourceabilityQaDecisionListener` → keep `NonSourceableVerification` (manual handling required)

## Shared Payload: NonSourceabilityLifecycleEvent
```kotlin
data class NonSourceabilityLifecycleEvent(
    val nonSourceabilityId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    val eventType: NonSourceabilityEventType,
)
```
Carried in JSON body. Correlation ID and message type are separate CloudEvent headers.

## Delivery and Processing Semantics
- Delivery model: at-least-once.
- Consumer requirement: idempotent processing for duplicates/replays.
- Validation: fail-fast on malformed identifiers; routes to dead-letter exchange on `MessageQueueRejectException`.
- Dead-letter handling: rejected malformed events must route to dead-letter flow via `x-dead-letter-exchange`.
- Deduplication utility: `EventDeduplicationService` available in `dataland-message-queue-utils` for optional in-process deduplication.

## Compatibility and Rollout
- Message schema evolution must be additive by default.
- Non-additive changes require coordinated producer-consumer rollout and migration notes.
- Backward compatibility checks are mandatory for routing keys, exchange names, and required fields.
