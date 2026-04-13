# Data Model: Non-Sourceability Deactivated by Dataset Upload

**Feature**: `008-nonsource-deactivated-by-dataset`

No new data model. The test reads and writes using existing OpenAPI client models:

| Model | Source | Fields used |
|---|---|---|
| `NonSourceabilityRequest` | `datalandbackend.openApiClient.model` | `companyId`, `dataType`, `reportingPeriod`, `reason`, `bypassQa` |
| `DataMetaInformation` | `datalandbackend.openApiClient.model` | `dataId`, `qaStatus`, `currentlyActive` (on non-sourceability side) |
| `QaStatus` (QA service) | `datalandqaservice.openApiClient.model` | `Accepted` |
| `NonSourceabilityInfo` | returned by `getInfoOnNonSourceabilityOfDatasets` | `currentlyActive`, `qaStatus` |

The pre-existing `Ctx` data class in `NonSourceabilityTest` is reused as-is:

```kotlin
data class Ctx(
    val companyId: String,
    val dataType: DataTypeEnum,
    val reportingPeriod: String,
    val dataSourcingId: String? = null,
)
```

No new entities, no schema changes.
