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
- Existing `nonSourceable` filter alias remains backward-compatible and maps to canonical `qaStatus/currentlyActive` semantics.

## Implemented Notes (2026-04-08)
- `POST /metadata/nonSourceable` now rejects duplicate `(companyId, dataType, reportingPeriod)` tuples when canonical records are in `Pending` or `Accepted` state.
- `POST /metadata/nonSourceable?bypassQa=true` enforces admin-only authorization and emits `NON_SOURCEABILITY_LIFECYCLE` with `eventType=AUTO_ACCEPTED`.
- `HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}` is backed by canonical `currentlyActive` state from backend non-sourceability records.

## Rollout Notes
1. Deploy backend accepting both old and new persistence mapping during migration window.
2. Apply DB migration for new entity/table/columns before strict read-path cutover.
3. Coordinate consumer deployment for dedicated bypass event before enabling bypass emission.
