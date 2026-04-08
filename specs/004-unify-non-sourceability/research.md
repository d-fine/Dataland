# Research: Unified Non-Sourceability Resolution

## Decision 1: Canonical uniqueness key
- Decision: Use `(companyId, dataType, reportingPeriod)` as the canonical uniqueness key for non-sourceability requests.
- Rationale: Existing backend sourceability and metadata search logic is already keyed by these dimensions, minimizing migration risk and avoiding dual-identity ambiguity.
- Alternatives considered:
  - `datasetId` as canonical key: rejected because non-sourceability is requested before a canonical accepted dataset instance may exist.
  - Dual key model (`datasetId` plus dimensions): rejected due to conflict and deduplication complexity.

## Decision 2: Bypass event contract
- Decision: Emit a dedicated `non-sourceability-auto-accepted` event when `bypassQa=true`.
- Rationale: Consumers can distinguish normal pending review creation from admin bypass acceptance without overloading one event type.
- Alternatives considered:
  - Reuse `non-sourceability-created` with accepted status: rejected due to weaker semantic separation.
  - Emit both created and accepted events: rejected because it increases duplicate/ordering complexity.

## Decision 3: Delivery semantics
- Decision: Require at-least-once delivery and idempotent consumers.
- Rationale: Matches RabbitMQ reliability assumptions and existing service patterns using replay-safe handlers.
- Alternatives considered:
  - Exactly-once end-to-end semantics: rejected as impractical without significant infrastructure and transaction-outbox scope expansion.
  - Best-effort delivery: rejected because it fails the durability and consistency requirements.

## Decision 4: Rejection behavior in data sourcing
- Decision: On QA rejection, keep data sourcing state in `NonSourceableVerification` for manual QA-team handling.
- Rationale: Preserves explicit operator control and prevents unintended automatic resourcing transitions.
- Alternatives considered:
  - Automatic return to normal sourcing state: rejected by clarified product decision.
  - New dedicated rejected sourcing state: rejected to avoid unnecessary state model expansion in this feature.

## Decision 5: Propagation SLO
- Decision: Use 60 seconds as the end-to-end propagation target from backend/QA emit to consumer state reflection.
- Rationale: Strict enough for operational assurance while realistic for asynchronous service hops.
- Alternatives considered:
  - 30 seconds: rejected as too strict under load/retry scenarios.
  - 5 minutes: rejected as too loose for user-visible consistency.

## Decision 6: Backend runtime entity rewiring scope
- Decision: Rewire metadata non-sourceability runtime flows to `NonSourceabilityInformationEntity` and associated API model wiring, while retaining `SourceabilityEntity` as backup-only data.
- Rationale: Establishes backend runtime single source of truth with explicit QA status and activity state without removing legacy backup records.
- Alternatives considered:
  - Keep using `SourceabilityEntity` for active endpoint processing: rejected because it cannot represent the full QA-driven lifecycle semantics cleanly.

## Decision 7: Data-sourcing state/authorization updates
- Decision: Add `NonSourceableVerification` to data-sourcing state handling and tighten patch authorization logic for explicit role/state combinations.
- Rationale: Supports event-driven pending verification semantics and enforces least-privilege state changes.
- Alternatives considered:
  - Reuse existing states only: rejected because pending verification is semantically distinct from confirmed non-sourceable.
