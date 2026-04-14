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
