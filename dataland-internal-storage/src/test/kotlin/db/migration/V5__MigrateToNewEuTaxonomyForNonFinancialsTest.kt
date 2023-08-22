package db.migration

import db.migration.utils.buildDatabaseEntry
import db.migration.utils.mockAndWhenConfigurationForFrameworkMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class V5__MigrateToNewEuTaxonomyForNonFinancialsTest {
    @Test
    fun `test that eu taxonomy for non financials migration script works as expected for migrating new data`() {
        val mockContext = Mockito.mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            buildDatabaseEntry(
                JSONObject(
                    "{" +
                        "\"something\": \"something\"" +
                        "}",
                ),
                "new-eutaxonomy-non-financials",
            ),
            buildDatabaseEntry(
                JSONObject(
                    "{" +
                        "\"something\": \"something\"" +
                        "}",
                ),
                "eutaxonomy-non-financials",
            ),
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrate(mockContext)
    }

    @Test
    fun `test that eu taxonomy for non financials migration script works as expected for migrating old data`() {
        val mockContext = Mockito.mock(Context::class.java)
        mockAndWhenConfigurationForFrameworkMigration(
            mockContext,
            oldOriginalDatabaseEntry,
            oldExpectedTransformedDataBaseEntry,
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrate(mockContext)
    }

    val oldOriginalDatabaseEntry = buildDatabaseEntry(
        JSONObject(
            "{" +
                "\"capex\": {" +
                "\"totalAmount\": null," +
                "\"alignedData\": {" +
                "\"valueAsPercentage\": 0.1965," +
                "\"quality\": \"Audited\"," +
                "\"dataSource\": {" +
                "\"report\": \"SustainabilityReport\"," +
                "\"page\": 1017," +
                "\"tagName\": \"platforms\"" +
                "}," +
                "\"comment\": \"quantify 1080p driver\"," +
                "\"valueAsAbsolute\": 396564.95871022344" +
                "}," +
                "\"eligibleData\": null" +
                "}," +
                "\"opex\": {" +
                "\"totalAmount\": {" +
                "\"quality\": \"Estimated\"," +
                "\"dataSource\": null," +
                "\"comment\": null," +
                "\"value\": 940716.1856535822" +
                "}," +
                "\"alignedData\": null," +
                "\"eligibleData\": null" +
                "}," +
                "\"revenue\": {" +
                "\"totalAmount\": {" +
                "\"quality\": null," +
                "\"dataSource\": {" +
                "\"report\": null," +
                "\"page\": null," +
                "\"tagName\": null" +
                "}," +
                "\"comment\": null," +
                "\"value\": 470269.4387175143" +
                "}," +
                "\"alignedData\": {" +
                "\"valueAsPercentage\": 0.4347," +
                "\"quality\": \"Reported\"," +
                "\"dataSource\": {" +
                "\"report\": \"SustainabilityReport\"," +
                "\"page\": 246," +
                "\"tagName\": \"communities\"" +
                "}," +
                "\"comment\": \"transmit online pixel\"," +
                "\"valueAsAbsolute\": 840897.8063846007" +
                "}," +
                "\"eligibleData\": {" +
                "\"valueAsPercentage\": null," +
                "\"quality\": \"Incomplete\"," +
                "\"dataSource\": {}," +
                "\"comment\": null," +
                "\"valueAsAbsolute\": 917185.3733132593" +
                "}" +
                "}," +
                "\"fiscalYearDeviation\": \"Deviation\"," +
                "\"fiscalYearEnd\": \"2022-10-05\"," +
                "\"scopeOfEntities\": \"NA\"," +
                "\"nfrdMandatory\": null," +
                "\"euTaxonomyActivityLevelReporting\": \"No\"," +
                "\"assurance\": {" +
                "\"assurance\": \"LimitedAssurance\"," +
                "\"provider\": null," +
                "\"dataSource\": {" +
                "\"report\": \"SustainabilityReport\"," +
                "\"page\": 91," +
                "\"tagName\": \"initiatives\"" +
                "}" +
                "}," +
                "\"numberOfEmployees\": null," +
                "\"referencedReports\": {" +
                "\"ESEFReport\": {" +
                "\"reference\": \"50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63\"," +
                "\"isGroupLevel\": null," +
                "\"reportDate\": null," +
                "\"currency\": \"BSD\"" +
                "}," +
                "\"SustainabilityReport\": {" +
                "\"reference\": \"50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63\"," +
                "\"isGroupLevel\": null," +
                "\"reportDate\": null," +
                "\"currency\": \"THB\"" +
                "}" +
                "}" +
                "}",
        ),
        "eutaxonomy-non-financials",
    )

    val oldExpectedTransformedDataBaseEntry = buildDatabaseEntry(
        JSONObject(
            "{" +
                "\"general\": {" +
                "\"fiscalYearDeviation\": \"Deviation\"," +
                "\"fiscalYearEnd\": \"2022-10-05\"," +
                "\"nfrdMandatory\": null," +
                "\"numberOfEmployees\": null," +
                "\"referencedReports\": {" +
                "\"SustainabilityReport\": {" +
                "\"reference\": \"50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63\"," +
                "\"reportDate\": null," +
                "\"isGroupLevel\": null," +
                "\"currency\": \"THB\"" +
                "}," +
                "\"ESEFReport\": {" +
                "\"reference\": \"50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63\"," +
                "\"reportDate\": null," +
                "\"isGroupLevel\": null," +
                "\"currency\": \"BSD\"" +
                "}" +
                "}," +
                "\"assurance\": {" +
                "\"assurance\": \"LimitedAssurance\"," +
                "\"provider\": null," +
                "\"dataSource\": {" +
                "\"page\": 91," +
                "\"report\": \"SustainabilityReport\"," +
                "\"tagName\": \"initiatives\"" +
                "}" +
                "}," +
                "\"scopeOfEntities\": \"NA\"," +
                "\"euTaxonomyActivityLevelReporting\": \"No\"" +
                "}," +
                "\"opex\": {" +
                "\"totalAmount\": {" +
                "\"comment\": null," +
                "\"dataSource\": null," +
                "\"value\": {" +
                "\"amount\": 940716.1856535822," +
                "\"currency\": null" +
                "}," +
                "\"quality\": \"Estimated\"" +
                "}," +
                "\"totalAlignedShare\": {}," +
                "\"totalEligibleShare\": {}," +
                "\"totalNonEligibleShare\": null," +
                "\"totalNonAlignedShare\": null," +
                "\"nonAlignedActivities\": null," +
                "\"substantialContributionCriteria\": null," +
                "\"alignedActivities\": null," +
                "\"totalEnablingShare\": null," +
                "\"totalTransitionalShare\": null," +
                "}," +
                "\"capex\": {" +
                "\"totalAlignedShare\": {" +
                "\"relativeShareInPercent\": 0.1965," +
                "\"absoluteShare\": {" +
                "\"amount\": 396564.95871022344," +
                "\"currency\": null" +
                "}" +
                "}," +
                "\"totalAmount\": {" +
                "\"value\": null," +
                "\"quality\": \"Audited\"," +
                "\"dataSource\": {" +
                "\"report\": \"SustainabilityReport\"," +
                "\"page\": 1017," +
                "\"tagName\": \"platforms\"" +
                "}," +
                "\"comment\": \"quantify 1080p driver\"" +
                "}," +
                "\"totalEligibleShare\": {}," +
                "\"totalNonEligibleShare\": null," +
                "\"totalNonAlignedShare\": null," +
                "\"nonAlignedActivities\": null," +
                "\"substantialContributionCriteria\": null," +
                "\"alignedActivities\": null," +
                "\"totalEnablingShare\": null," +
                "\"totalTransitionalShare\": null," +
                "}," +
                "\"revenue\": {" +
                "\"totalAmount\": {" +
                "\"dataSource\": {}," +
                "\"value\": {" +
                "\"amount\": 470269.4387175143," +
                "\"currency\": null " +
                "}," +
                "\"quality\": \"Incomplete\"," +
                "\"comment\": null" +
                "}," +
                "\"totalAlignedShare\": {" +
                "\"relativeShareInPercent\": 0.4347," +
                "\"absoluteShare\": {" +
                "\"amount\": 840897.8063846007," +
                "\"currency\": null" +
                "}" +
                "}," +
                "\"totalEligibleShare\": {" +
                "\"relativeShareInPercent\": null," +
                "\"absoluteShare\": {" +
                "\"amount\": 917185.3733132593," +
                "\"currency\": null" +
                "}" +
                "}," +
                "\"totalNonEligibleShare\": null," +
                "\"totalNonAlignedShare\": null," +
                "\"nonAlignedActivities\": null," +
                "\"substantialContributionCriteria\": null," +
                "\"alignedActivities\": null," +
                "\"totalEnablingShare\": null," +
                "\"totalTransitionalShare\": null," +
                "}" +
                "}",
        ),
        "eutaxonomy-non-financials",
    )
}
