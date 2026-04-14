# Research: Non-Sourceability Deactivated by Dataset Upload

**Feature**: `008-nonsource-deactivated-by-dataset`

## No Unknowns

All APIs used in the new test are already exercised in the existing test suite:

| API | Used in |
|---|---|
| `metaDataControllerApi.postNonSourceabilityOfADataset` | `NonSourceabilityTest` — `postNonSourceableWithBypassQa` |
| `apiAccessor.uploadDummyFrameworkDataset(companyId, DataTypeEnum.sfdr, period, bypassQa=false)` | `DataSourcingServiceListenerTest.uploadDummyDataForDataSourcingObject` |
| `qaServiceControllerApi.changeQaStatus(dataId, QaStatus.Accepted)` | `DataSourcingServiceListenerTest`, `CommunityManagerListenerTest`, `DataDeletionControllerTest` |
| `metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets` | `NonSourceabilityTest` — all existing tests |
| `awaitUntilAsserted` | Used throughout `NonSourceabilityTest` |

## Decisions

| Decision | Rationale |
|---|---|
| Use `apiAccessor.uploadDummyFrameworkDataset(bypassQa=false)` for dataset upload | Already does auth setup and returns `DataMetaInformation` with `dataId`. |
| Use `apiAccessor.qaServiceControllerApi.changeQaStatus` for QA approval | Consistent with all other tests that approve datasets in this suite. |
| Wrap the `currentlyActive=false` assertion in `awaitUntilAsserted` | QA acceptance propagates asynchronously via RabbitMQ, same as existing `assertBackendEntryIsAcceptedAndActive`. |
| Initialize non-sourceability with `bypassQa=true` | Gives a cleanly `Accepted + currentlyActive=true` entry without extra QA steps, exactly as `POST nonSourceable with bypassQa true` test does. |
