# Quickstart: Non-Sourceability Deactivated by Dataset Upload

**Feature**: `008-nonsource-deactivated-by-dataset`

## What to implement

Add the following to `NonSourceabilityTest.kt` (file: `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt`):

### 1. New `@Test` method

```kotlin
@Test
fun `currentlyActive becomes false after QA approves a dataset for the same triple`() {
    val ctx = Ctx(
        companyId = asAdmin { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId },
        dataType = DataTypeEnum.sfdr,
        reportingPeriod = testReportingPeriod,
    )

    // Step 0: establish an active non-sourceability entry
    postNonSourceableWithBypassQa(ctx)
    assertBackendEntryIsAcceptedAndActive(ctx)

    // Step 1 + 2: upload dataset, then QA-approve it
    val dataId = uploadDatasetForTriple(ctx)
    asAdmin { apiAccessor.qaServiceControllerApi.changeQaStatus(dataId, QaServiceQaStatus.Accepted) }

    // Step 3: non-sourceability entry must now be inactive
    assertNonSourceabilityIsInactive(ctx)
}
```

### 2. New private helper: `uploadDatasetForTriple`

```kotlin
private fun uploadDatasetForTriple(ctx: Ctx): String =
    asAdmin {
        apiAccessor.uploadDummyFrameworkDataset(
            companyId = ctx.companyId,
            dataType = ctx.dataType,
            reportingPeriod = ctx.reportingPeriod,
            bypassQa = false,
        ).dataId
    }
```

### 3. New private helper: `assertNonSourceabilityIsInactive`

```kotlin
private fun assertNonSourceabilityIsInactive(ctx: Ctx) {
    awaitUntilAsserted {
        val entries = asAdmin {
            apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                companyId = ctx.companyId,
                dataType = ctx.dataType,
                reportingPeriod = ctx.reportingPeriod,
            )
        }
        assertFalse(entries.first().currentlyActive, "currentlyActive must be false after QA accepts a dataset for the same triple")
    }
}
```

### Import note

`QaServiceQaStatus` is already imported at the top of the file:
```kotlin
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaServiceQaStatus
```

No new imports needed.

## How to run

```bash
# Full local stack must be running first
./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.currentlyActive becomes false after QA approves a dataset for the same triple"
```
