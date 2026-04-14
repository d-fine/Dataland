# Data Model: Unified Non-Sourceability Resolution

## Entity: NonSourceabilityInformationEntity (Backend)
- Purpose: Authoritative persistence for non-sourceability lifecycle.
- Fields:
  - `nonSourceabilityId` (UUID, primary key)
  - `companyId` (string, required)
  - `dataType` (enum, required)
  - `reportingPeriod` (string, required)
  - `qaStatus` (enum: `Pending|Accepted|Rejected`, required)
  - `uploaderUserId` (string, required)
  - `uploadTime` (timestamp/epoch, required)
  - `currentlyActive` (boolean, required)
  - `reason` (string, required)
  - `bypassQa` (boolean, required)
- Uniqueness and constraints:
  - At most one active row for `(companyId, dataType, reportingPeriod)`.
  - POST reject rule: reject creation when an existing row for same tuple has `qaStatus in {Pending, Accepted}`.
- State transitions:
  - POST with `bypassQa=false`: `qaStatus=Pending`, `currentlyActive=false`.
  - POST with `bypassQa=true`: `qaStatus=Accepted`, `currentlyActive=true`.
  - QA acceptance event: set `qaStatus=Accepted`, `currentlyActive=true`.
  - QA rejection event: set `qaStatus=Rejected`, `currentlyActive=false`.

## Entity: NonSourceableQaReviewInformation (QA Service)
- Purpose: QA review representation for non-sourceability requests.
- Fields:
  - `nonSourceabilityId` (UUID/string, foreign reference to backend record)
  - `companyId` (string, required)
  - `dataType` (enum, required)
  - `reportingPeriod` (string, required)
  - `qaStatus` (enum: `Pending|Accepted|Rejected`, required)
  - `reason` (string, required)
  - `uploaderUserId` (string, required)
  - `uploadTime` (timestamp/epoch, required)
  - `reviewerUserId` (string, nullable until decision)
  - `qaComment` (string, nullable)
- Lifecycle:
  - Created from backend non-sourceability-created event.
  - Updated by `POST /nonSourceable/{nonSourceabilityId}` decision.

## Aggregate: DataSourcingObject (Data-Sourcing Service)
- Purpose: Data sourcing lifecycle state for a data dimension/request.
- Relevant states:
  - Existing: `DocumentSourcing`, `DocumentSourcingDone`, `DataExtraction`, `DataVerification`, `NonSourceable`, `Done`, ...
  - New/extended handling: `NonSourceableVerification`.
- State transitions for this feature:
  - Backend non-sourceability-created event -> `NonSourceableVerification`.
  - Backend non-sourceability-auto-accepted event or QA accepted event -> `NonSourceable`.
  - QA rejected event -> remain at `NonSourceableVerification` for manual handling.

## Message Contracts and Correlation
- Required correlation key on all related messages and payloads: `nonSourceabilityId`.
- Idempotency keying expectation:
  - Backend/QA updates keyed by `nonSourceabilityId`.
  - Data-sourcing transitions keyed by `(nonSourceabilityId, targetState)`.

## Validation Rules
- `bypassQa=true` allowed only for admin role.
- Reject malformed identifiers and unknown event references with fail-fast logging.
- All event handlers must be replay-safe and duplicate-safe.
