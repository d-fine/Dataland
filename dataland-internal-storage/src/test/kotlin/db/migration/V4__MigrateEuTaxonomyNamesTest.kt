package db.migration

import db.migration.utils.DataTableEntity
import org.dataland.datalandbackend.openApiClient.model.YesNo
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V4__MigrateEuTaxonomyNamesTest {
    @Test
    fun `test that eu taxonomy for non financials migration script works as expected`() {
        testIfDataIsMaintained(YesNo.Yes, YesNo.No, "eutaxonomy-non-financials")
    }

    @Test
    fun `test that eu taxonomy for financials migration script works as expected`() {
        testIfDataIsMaintained(YesNo.Yes, YesNo.No, "eutaxonomy-financials")
    }

    @Test
    fun `test that eu taxonomy migration script works as expected with unprovided values`() {
        testIfDataIsMaintained(YesNo.Yes, null, "eutaxonomy-non-financials")
    }

    private fun testIfDataIsMaintained(
        activityLevelReporting: YesNo?,
        reportingObligation: YesNo?,
        dataType: String,
    ) {
        val originalDataTableEntity = buildOriginalDatabaseEntry(activityLevelReporting, reportingObligation, dataType)
        val expectedDataTableEntity =
            buildExpectedTransformedDatabaseEntry(
                activityLevelReporting, reportingObligation, dataType,
            )
        val migration = V4__MigrateEuTaxonomyNames()
        migration.migrateEuTaxonomyNames(originalDataTableEntity)

        Assertions.assertEquals(originalDataTableEntity, expectedDataTableEntity)
    }

    private fun buildOriginalDatabaseEntry(
        activityLevelReporting: YesNo?,
        reportingObligation: YesNo?,
        dataType: String,
    ): DataTableEntity {
        val simplifiedDataset =
            JSONObject(
                "{" +
                    "\"activityLevelReporting\": ${convertYesNoToJsonValue(activityLevelReporting)}," +
                    "\"reportingObligation\": ${convertYesNoToJsonValue(reportingObligation)}," +
                    "\"somethingElse\": \"No\"" +
                    "}",
            )
        return DataTableEntity.fromJsonObject("mock-data-id", dataType, simplifiedDataset)
    }

    private fun buildExpectedTransformedDatabaseEntry(
        euTaxonomyActivityLevelReporting: YesNo?,
        nfrdMandatory: YesNo?,
        dataType: String,
    ): DataTableEntity {
        val simplifiedDataset =
            JSONObject(
                "{" +
                    "\"euTaxonomyActivityLevelReporting\": ${convertYesNoToJsonValue(euTaxonomyActivityLevelReporting)}," +
                    "\"nfrdMandatory\": ${convertYesNoToJsonValue(nfrdMandatory)}," +
                    "\"somethingElse\": \"No\"" +
                    "}",
            )
        return DataTableEntity.fromJsonObject("mock-data-id", dataType, simplifiedDataset)
    }

    private fun convertYesNoToJsonValue(yesNo: YesNo?): String = if (yesNo != null) "\"$yesNo\"" else "null"
}
