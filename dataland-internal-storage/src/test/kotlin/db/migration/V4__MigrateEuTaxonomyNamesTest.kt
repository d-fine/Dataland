package db.migration

import db.migration.utils.buildDatabaseEntry
import db.migration.utils.mockAndWhenConfigurationForFrameworkMigration
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.YesNo
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class V4__MigrateEuTaxonomyNamesTest {

    @Test
    fun `test that eu taxonomy for non financials migration script works as expected`() {
        testIfDataIsMaintained(YesNo.yes, YesNo.no, "eutaxonomy-non-financials")
    }

    @Test
    fun `test that eu taxonomy for financials migration script works as expected`() {
        testIfDataIsMaintained(YesNo.yes, YesNo.no, "eutaxonomy-financials")
    }

    @Test
    fun `test that eu taxonomy migration script works as expected with unprovided values`() {
        testIfDataIsMaintained(YesNo.yes, null, "eutaxonomy-non-financials")
    }

    private fun testIfDataIsMaintained(
        activityLevelReporting: YesNo?,
        reportingObligation: YesNo?,
        dataType: String,
    ) {
        val mockContext = Mockito.mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            buildOriginalDatabaseEntry(activityLevelReporting, reportingObligation, dataType),
            buildExpectedTransformedDatabaseEntry(activityLevelReporting, reportingObligation, dataType),
        )
        val migration = V4__MigrateEuTaxonomyNames()
        migration.migrate(mockContext)
    }

    private fun buildOriginalDatabaseEntry(
        activityLevelReporting: YesNo?,
        reportingObligation: YesNo?,
        dataType: String,
    ): String {
        val simplifiedDataset = JSONObject(
            "{" +
                "\"activityLevelReporting\": ${convertYesNoToJsonValue(activityLevelReporting)}," +
                "\"reportingObligation\": ${convertYesNoToJsonValue(reportingObligation)}," +
                "\"somethingElse\": \"No\"" +
                "}",
        )
        return buildDatabaseEntry(simplifiedDataset, dataType)
    }

    private fun buildExpectedTransformedDatabaseEntry(
        euTaxonomyActivityLevelReporting: YesNo?,
        nfrdMandatory: YesNo?,
        dataType: String,
    ): String {
        val simplifiedDataset = JSONObject(
            "{" +
                "\"euTaxonomyActivityLevelReporting\": ${convertYesNoToJsonValue(euTaxonomyActivityLevelReporting)}," +
                "\"nfrdMandatory\": ${convertYesNoToJsonValue(nfrdMandatory)}," +
                "\"somethingElse\": \"No\"" +
                "}",
        )
        return buildDatabaseEntry(simplifiedDataset, dataType)
    }

    private fun convertYesNoToJsonValue(yesNo: YesNo?): String = if (yesNo != null) "\"$yesNo\"" else "null"
}
