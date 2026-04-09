# Data Model: NonSourceability QA Lifecycle E2E Test

This feature adds no new entities or persistence. It exercises existing models through already-generated OpenAPI clients.

## Models consumed by the test

### NonSourceabilityRequest *(backend — POST body)*

| Field | Type | Description |
|---|---|---|
| `companyId` | `String` | ID of the company |
| `dataType` | `DataTypeEnum` | Framework identifier (e.g. `sfdr`) |
| `reportingPeriod` | `String` | Year string (e.g. `"2026"`) |
| `reason` | `String` | Human-readable justification |
| `bypassQa` | `Boolean` | `false` → entry enters QA review; `true` (admin-only) → immediately active |

### NonSourceabilityInformationResponse *(backend — GET/POST response)*

| Field | Type | Description |
|---|---|---|
| `nonSourceabilityId` | `String` | UUID of the entry (used to call QA service POST) |
| `companyId` | `String` | — |
| `dataType` | `DataTypeEnum` | — |
| `reportingPeriod` | `String` | — |
| `qaStatus` | `QaStatus` (backend) | `Pending` → `Accepted` / `Rejected` |
| `currentlyActive` | `Boolean` | `false` until QA accepts; `true` after acceptance |
| `uploaderUserId` | `String` | — |
| `uploadTime` | `Long` | Unix ms timestamp |

### NonSourceableQaReviewInformation *(QA service — GET/POST response)*

| Field | Type | Description |
|---|---|---|
| `nonSourceabilityId` | `String` | Shared identifier with backend entry |
| `companyId` | `String` | — |
| `dataType` | `String` | String form (e.g. `"sfdr"`) — differs from backend's `DataTypeEnum` |
| `reportingPeriod` | `String` | — |
| `qaStatus` | `QaStatus` (QA service) | `Pending` → `Accepted` / `Rejected` |
| `reason` | `String?` | Uploader's reason |
| `uploaderUserId` | `String` | — |
| `uploadTime` | `Long` | — |
| `reviewerUserId` | `String?` | Null until reviewed |
| `qaComment` | `String?` | Null unless reviewer adds comment |

## State transitions tested

```
[backend] POST /metadata/nonSourceable (bypassQa=false)
  → NonSourceabilityInformationResponse { qaStatus=Pending, currentlyActive=false }
        ↓ (RabbitMQ event: non-sourceability-created)
[qa-service] NonSourceableQaReviewInformation { qaStatus=Pending }
  → POST /nonSourceable/{id} (qaStatus=Accepted)
        ↓ (RabbitMQ event: non-sourceability-qa-accepted)
[backend] NonSourceabilityInformationResponse { qaStatus=Accepted, currentlyActive=true }
```

## Package map *(for imports in test)*

| Type | Import |
|---|---|
| `NonSourceabilityRequest` | `org.dataland.datalandbackend.openApiClient.model.NonSourceabilityRequest` |
| `NonSourceabilityInformationResponse` | `org.dataland.datalandbackend.openApiClient.model.NonSourceabilityInformationResponse` |
| `DataTypeEnum` | `org.dataland.datalandbackend.openApiClient.model.DataTypeEnum` |
| `QaStatus` (backend) | `org.dataland.datalandbackend.openApiClient.model.QaStatus` |
| `NonSourceableQaReviewInformation` | `org.dataland.datalandqaservice.openApiClient.model.NonSourceableQaReviewInformation` |
| `QaStatus` (QA service) | `org.dataland.datalandqaservice.openApiClient.model.QaStatus` |
| `awaitUntilAsserted` | `org.dataland.e2etests.utils.testDataProviders.awaitUntilAsserted` |
