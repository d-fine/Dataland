# Quickstart: Unified Non-Sourceability Resolution

## Prerequisites
- Running local stack with backend, qa-service, data-sourcing-service, postgres, and rabbitmq.
- Valid auth tokens for role-based tests:
  - standard uploader/user
  - admin user

## 1. Start services
1. Start local stack using repository standard startup workflow.
2. Verify RabbitMQ and three target services are healthy.

## 2. Create non-sourceability request (normal flow)
1. Call `POST /metadata/nonSourceable` with:
   - query parameter: `bypassQa=false`
   - body: `{companyId, dataType, reportingPeriod, reason}`
2. Verify backend persistence:
   - `qaStatus=Pending`
   - `currentlyActive=false`
3. Verify QA service created `NonSourceableQaReviewInformation` row.
4. Verify data-sourcing state changed to `NonSourceableVerification`.

## 3. Approve request in QA service
1. Call `POST /nonSourceable/{nonSourceabilityId}` with:
   - `qaStatus=Accepted`
   - optional `qaComment`
2. Verify backend record updated to:
   - `qaStatus=Accepted`
   - `currentlyActive=true`
3. Verify data-sourcing state changed to `NonSourceable`.
4. Verify end-to-end propagation happened within 60 seconds.

## 4. Reject request in QA service
1. Create a fresh pending request (step 2).
2. Call `POST /nonSourceable/{nonSourceabilityId}` with:
   - `qaStatus=Rejected`
   - optional `qaComment`
3. Verify backend record updated to:
   - `qaStatus=Rejected`
   - `currentlyActive=false`
4. Verify data-sourcing remains in `NonSourceableVerification` for manual handling.

## 5. Bypass flow
1. Call `POST /metadata/nonSourceable` with admin token and `bypassQa=true`.
2. Verify backend record is immediately active and accepted.
3. Verify no QA review row is created.
4. Verify dedicated `non-sourceability-auto-accepted` event is emitted and data-sourcing transitions to `NonSourceable`.

## 6. Authorization checks
1. Call bypass flow with non-admin token.
2. Verify authorization error and no persistence side effects.

## 7. Duplicate and idempotency checks
1. Submit duplicate POST for active/pending tuple `(companyId, dataType, reportingPeriod)`.
2. Verify backend rejects duplicate.
3. Replay accepted/rejected events.
4. Verify no duplicate records and no inconsistent state transitions.

## 8. Contract and compatibility checks
1. Validate OpenAPI changes for backend and QA non-sourceability endpoints.
2. Validate message schema documentation for:
   - `non-sourceability-created`
   - `non-sourceability-auto-accepted`
   - QA accepted/rejected events
3. Confirm additive compatibility notes are documented.

## Validation Outcomes (2026-04-08)
- Verified by targeted backend, QA, and data-sourcing integration tests:
   - Backend canonical lifecycle endpoint/consumer behavior:
      - `:dataland-backend:test --tests org.dataland.datalandbackend.controller.MetaDataControllerNonSourceableTest`
      - `:dataland-backend:test --tests org.dataland.datalandbackend.services.SourceabilityDataManagerTest`
      - `:dataland-backend:test --tests org.dataland.datalandbackend.services.NonSourceabilityQaDecisionConsumerTest`
   - QA lifecycle ingestion and decision behavior:
      - `:dataland-qa-service:test --tests org.dataland.datalandqaservice.services.NonSourceabilityEventListenerTest`
      - `:dataland-qa-service:test --tests org.dataland.datalandqaservice.controller.NonSourceabilityQaControllerTest`
   - Data-sourcing lifecycle transitions and state patch authorization:
      - `:dataland-data-sourcing-service:test --tests org.dataland.datasourcingservice.serviceTests.NonSourceabilityEventConsumerTest`
      - `:dataland-data-sourcing-service:test --tests org.dataland.datasourcingservice.serviceTests.NonSourceabilityQaAcceptedConsumerTest`
      - `:dataland-data-sourcing-service:test --tests org.dataland.datasourcingservice.serviceTests.NonSourceabilityQaRejectedConsumerTest`
      - `:dataland-data-sourcing-service:test --tests org.dataland.datasourcingservice.serviceTests.DataSourcingControllerTest`
- Result summary:
   - Request -> QA accepted -> backend active + data-sourcing `NonSourceable` path validated.
   - Request -> QA rejected -> backend rejected/inactive + data-sourcing `NonSourceableVerification` path validated.
   - Bypass authorization and duplicate-request guards validated via backend service/controller tests.
- Operational note: full local-stack manual API walkthrough can be executed later; current implementation criteria are covered by module-level integration tests.
