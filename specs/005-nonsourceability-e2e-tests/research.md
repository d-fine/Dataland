# Research: NonSourceability QA Lifecycle E2E Test

## 1. Existing test skeleton

**Decision**: Extend `NonSourceabilityTest.kt` by replacing/filling the existing commented-out skeleton rather than creating a new file.  
**Rationale**: The file already exists with the `@TestInstance(PER_CLASS)` annotation, an `ApiAccessor` field, and a `testReportingPeriod`. One test method is partially written (`bypassQa=true`). Adding the `bypassQa=false` lifecycle test alongside it is the minimal change.  
**Alternatives considered**: New file — rejected because the skeleton is clearly intended to hold this test.

## 2. API clients available in e2etests

### Backend (via `apiAccessor.metaDataControllerApi`)

| Method | Signature | Notes |
|---|---|---|
| POST | `postNonSourceabilityOfADataset(NonSourceabilityRequest) : NonSourceabilityInformationResponse` | Returns full response including `nonSourceabilityId`, `qaStatus`, `currentlyActive` |
| GET | `getInfoOnNonSourceabilityOfDatasets(companyId, dataType: DataTypeEnum?, reportingPeriod, qaStatus?) : List<NonSourceabilityInformationResponse>` | All params optional |

### QA Service (via `apiAccessor.nonSourceabilityQaControllerApi`)

| Method | Signature | Notes |
|---|---|---|
| GET queue | `getNonSourceableQueue() : List<NonSourceableQaReviewInformation>` | Pending entries only |
| GET filtered | `getNonSourceableReviews(companyId?, dataType: String?, reportingPeriod?, qaStatus?, chunkSize?, chunkIndex?) : List<NonSourceableQaReviewInformation>` | dataType is `String?` not `DataTypeEnum` |
| POST decision | `postNonSourceabilityDecision(nonSourceabilityId, qaStatus: QaStatus, qaComment?) : NonSourceableQaReviewInformation` | QaStatus from `datalandqaservice.openApiClient.model` |

**Finding**: The `getNonSourceableReviews` takes `dataType: String?`, not `DataTypeEnum`, so `DataTypeEnum.sfdr.value` must be used (or map via `.value`).

## 3. Auth / user roles

**Pattern used throughout e2etests**:
```kotlin
GlobalAuth.withTechnicalUser(TechnicalUser.Admin) { ... }
```
- `TechnicalUser.Admin` has full permissions including `bypassQa=true` and QA decisions.
- `TechnicalUser.Reviewer` has the `ROLE_REVIEWER` needed for QA decisions — but Admin also works.
- **Decision**: Use `TechnicalUser.Admin` throughout the test for simplicity. Roles are verified in service-level unit tests; the E2E test is about the flow, not ACL.

## 4. Asynchronous assertion pattern

`awaitUntilAsserted` is from `org.dataland.e2etests.utils.testDataProviders`:
```kotlin
internal fun awaitUntilAsserted(operation: () -> Any) =
    Awaitility.await()
        .atMost(2000, TimeUnit.MILLISECONDS)
        .pollDelay(500, TimeUnit.MILLISECONDS)
        .untilAsserted { operation() }
```

**Decision**: Use `awaitUntilAsserted` for:
1. The GET from QA service (QA row must appear after backend emits event).
2. The final GET from backend (backend `qaStatus`/`currentlyActive` must update after QA service emits event).

Synchronous assertions for:
- The POST response body (returned immediately).
- The immediate GET from backend (no message queue hop needed).

## 5. Company creation

```kotlin
val companyId = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
    apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
}
```

`uploadOneCompanyWithRandomIdentifier()` sets auth internally, but wrapping it in `withTechnicalUser` is the standard pattern and keeps consistency. Auth set by helper call remains valid.

## 6. nonSourceabilityId flow

The `postNonSourceabilityOfADataset` return value (`NonSourceabilityInformationResponse`) contains `nonSourceabilityId: String`. This is the identifier passed to `postNonSourceabilityDecision` in the QA service.

**Decision**: Capture the POST response and extract `nonSourceabilityId` directly — no need to look it up separately.

## 7. DataTypeEnum string representation

`getNonSourceableReviews` expects `dataType: String?`. The string form of `DataTypeEnum.sfdr` is `"sfdr"` (its `.value`). Use `DataTypeEnum.sfdr.value` when calling this method.

## 8. Model packages

| Type | Package |
|---|---|
| `NonSourceabilityRequest` | `org.dataland.datalandbackend.openApiClient.model` |
| `NonSourceabilityInformationResponse` | `org.dataland.datalandbackend.openApiClient.model` |
| `DataTypeEnum` | `org.dataland.datalandbackend.openApiClient.model` |
| `QaStatus` (backend) | `org.dataland.datalandbackend.openApiClient.model` |
| `NonSourceableQaReviewInformation` | `org.dataland.datalandqaservice.openApiClient.model` |
| `QaStatus` (QA service) | `org.dataland.datalandqaservice.openApiClient.model` |

**Note**: Two `QaStatus` types exist — one in `backend` package, one in `qaservice` package. They must be imported unambiguously (e.g., aliased or fully qualified in the call site).

## 9. No new dependencies or infrastructure needed

All required API clients are already wired in `ApiAccessor`. No new Gradle dependencies. No new test utilities. The single test lives in the existing test class.
