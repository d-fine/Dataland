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

## Implementation Status

All three endpoints and the event listener are implemented:

| File | Role |
|---|---|
| `NonSourceabilityQaApi` | API interface defining 3 endpoints |
| `NonSourceabilityQaController` | Spring `@RestController` implementation |
| `NonSourceabilityQaReviewManager` | Decision service — emits accepted/rejected events |
| `NonSourceabilityEventListener` | RabbitMQ listener creating QA review rows |
| `NonSourceableQaReviewInformationEntity` | JPA entity (`non_sourceable_qa_review_information` table) |
| `NonSourceableQaReviewRepository` | JPA repository with filter queries |
| `V12__CreateNonSourceableQaReviewInformation` | Flyway Java migration |
| `NonSourceableQaReviewInformation` | Response model |

## Compatibility Notes
- New endpoints are additive and do not replace existing generic QA dataset endpoints immediately.
- Decision event payload remains backward-compatible by additive fields and stable identifiers.
