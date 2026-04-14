# Backend Metadata Non-Sourceability Contract

## Scope
- Service: `dataland-backend`
- Controller/API: metadata non-sourceability endpoints
- Consumers:
  - Frontend/admin operators
  - `dataland-qa-service` (via emitted events)
  - `dataland-data-sourcing-service` (via emitted events)

## Endpoints

### GET /metadata/nonSourceable
- Query parameters:
  - `companyId` (optional)
  - `dataType` (optional)
  - `reportingPeriod` (optional)
  - `qaStatus` (optional)
- Response: `200` list of `NonSourceabilityInformation`.

### POST /metadata/nonSourceable
- Query parameters:
  - `bypassQa` (optional, default false; true requires admin role)
- Request body:
  - `companyId` (required)
  - `dataType` (required)
  - `reportingPeriod` (required)
  - `reason` (required)
- Business rules:
  - Reject when active/pending row exists for same `(companyId, dataType, reportingPeriod)`.
  - `bypassQa=false`: create pending inactive row and emit `non-sourceability-created`.
  - `bypassQa=true`: create accepted active row and emit `non-sourceability-auto-accepted`.
- Responses:
  - `200/201` success (implementation-specific final status code)
  - `400` validation failure / duplicate rule violation
  - `401/403` unauthorized bypass attempt

### HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}
- Behavior update:
  - check `currentlyActive` for latest relevant non-sourceability row.
- Responses:
  - `200` if currently non-sourceable/active
  - `404` if no active non-sourceability

## Model: NonSourceabilityInformation
- `nonSourceabilityId`
- `companyId`
- `dataType`
- `reportingPeriod`
- `qaStatus`
- `uploaderUserId`
- `uploadTime`
- `currentlyActive`
- `reason`

## Compatibility Notes
- Existing endpoint paths are preserved.
- Response/request schema is additive migration from legacy sourceability payloads.
- Runtime endpoint semantics are moved to a QA-aware `NonSourceabilityInformation` model while `SourceabilityEntity` is retained as backup-only persistence.

## Implementation Status\n\nAll three endpoints are implemented in `MetaDataController` and `MetaDataApi` using canonical models:\n\n| File | Role |\n|---|---|\n| `NonSourceabilityInformationManager` | Core service — validation, persistence, event emission |\n| `NonSourceabilityInformationEntity` | JPA entity (`non_sourceability_information` table) |\n| `NonSourceabilityDataRepository` | JPA repository with JPQL filter queries |\n| `V13__CreateNonSourceabilityInformation` | Flyway Java migration |\n| `NonSourceabilityRequest` | POST request model |\n| `NonSourceabilityInformationResponse` | GET/POST response model |\n\n`SourceabilityEntity` is retained as backup-only persistence; the canonical read/write path uses `NonSourceabilityInformationEntity`.\n\n