# Non-Sourceability Messaging Contract

## Exchanges and Event Types

### Event A: non-sourceability-created
- Producer: `dataland-backend`
- Emitted when: `POST /metadata/nonSourceable` with `bypassQa=false`
- Consumers:
  - `dataland-qa-service` (create `NonSourceableQaReviewInformation`)
  - `dataland-data-sourcing-service` (set state `NonSourceableVerification`)

### Event B: non-sourceability-auto-accepted
- Producer: `dataland-backend`
- Emitted when: `POST /metadata/nonSourceable` with `bypassQa=true`
- Consumers:
  - `dataland-data-sourcing-service` (set state `NonSourceable`)

### Event C: non-sourceability-qa-accepted
- Producer: `dataland-qa-service`
- Emitted when: `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Accepted`
- Consumers:
  - `dataland-backend` (set `qaStatus=Accepted`, `currentlyActive=true`)
  - `dataland-data-sourcing-service` (set state `NonSourceable`)

### Event D: non-sourceability-qa-rejected
- Producer: `dataland-qa-service`
- Emitted when: `POST /nonSourceable/{nonSourceabilityId}` with `qaStatus=Rejected`
- Consumers:
  - `dataland-backend` (set `qaStatus=Rejected`, `currentlyActive=false`)
  - `dataland-data-sourcing-service` (keep `NonSourceableVerification` for manual handling)

## Shared Payload Requirements
- Required identifiers:
  - `nonSourceabilityId`
  - `companyId`
  - `dataType`
  - `reportingPeriod`
- Required status dimensions:
  - `qaStatus` where relevant
  - `currentlyActive` where relevant
- Required traceability metadata:
  - correlation identifier in headers
  - event type in headers

## Delivery and Processing Semantics
- Delivery model: at-least-once.
- Consumer requirement: idempotent processing for duplicates/replays.
- Validation: fail-fast on malformed identifiers and payload shape errors.
- Dead-letter handling: rejected malformed events must route to dead-letter flow.

## Compatibility and Rollout
- Message schema evolution must be additive by default.
- Non-additive changes require coordinated producer-consumer rollout and migration notes.
- Backward compatibility checks are mandatory for routing keys, exchange names, and required fields.
