# QA Non-Sourceability Contract

## Scope
- Service: `dataland-qa-service`
- Controller/API: non-sourceability QA endpoints
- Consumers:
  - QA reviewers
  - `dataland-backend` and `dataland-data-sourcing-service` via emitted decision events

## Endpoints

### GET /nonSourceable
- Query parameters:
  - `companyId` (optional)
  - `dataType` (optional)
  - `reportingPeriod` (optional)
  - `qaStatus` (optional)
  - `showOnlyActive` (optional)
  - `chunkSize` (optional)
  - `chunkIndex` (optional)
- Response: list of `NonSourceableQaReviewInformation`.

### GET /nonSourceable/queue
- Query parameters: none
- Behavior: equivalent to `/nonSourceable` filtered with `qaStatus=Pending`.
- Response: list of `NonSourceableQaReviewInformation` pending review entries.

### POST /nonSourceable/{nonSourceabilityId}
- Path parameter: `nonSourceabilityId` (required)
- Request parameters/body fields:
  - `qaStatus` (`Accepted` or `Rejected`)
  - `qaComment` (optional)
- Behavior:
  - update QA review entry
  - emit decision event consumed by backend and data-sourcing-service
- Response: success (`200` or `201`, implementation-finalized in API codegen)

## Model: NonSourceableQaReviewInformation
- `nonSourceabilityId`
- `companyId`
- `dataType`
- `reportingPeriod`
- `qaStatus`
- `reason`
- `uploaderUserId`
- `uploadTime`
- `reviewerUserId`
- `qaComment`

## Listener Contract
- Consumes `non-sourceability-created` event from backend.
- Creates one QA review row linked by `nonSourceabilityId`.

## Compatibility Notes
- New endpoints are additive and do not replace existing generic QA dataset endpoints immediately.
- Decision event payload remains backward-compatible by additive fields and stable identifiers.

## Implemented Notes (2026-04-08)
- `GET /nonSourceable` and `GET /nonSourceable/queue` are wired to canonical `NonSourceableQaReviewInformation` persistence and support filtering by dimensions and `qaStatus`.
- `POST /nonSourceable/{nonSourceabilityId}` accepts both `Accepted` and `Rejected` decisions, persists reviewer metadata/comment, and emits lifecycle events with:
  - `eventType=QA_ACCEPTED` and `currentlyActive=true`
  - `eventType=QA_REJECTED` and `currentlyActive=false`
- CREATED lifecycle events from backend create pending QA review rows keyed by `nonSourceabilityId`.
