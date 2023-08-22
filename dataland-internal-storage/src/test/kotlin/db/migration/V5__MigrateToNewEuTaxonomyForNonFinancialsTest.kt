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
            buildOldOriginalDatabaseEntry(),
            buildOldExpectedTransformedDatabaseEntry(),
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrate(mockContext)
    }

    private fun buildOldOriginalDatabaseEntry(): String {
        val dataObject = JSONObject(
            "{\n" +
                "    \"capex\": {\n" +
                "      \"totalAmount\": null,\n" +
                "      \"alignedData\": {\n" +
                "        \"valueAsPercentage\": 0.1965,\n" +
                "        \"quality\": \"Audited\",\n" +
                "        \"dataSource\": {\n" +
                "          \"report\": \"SustainabilityReport\",\n" +
                "          \"page\": 1017,\n" +
                "          \"tagName\": \"platforms\"\n" +
                "        },\n" +
                "        \"comment\": \"quantify 1080p driver\",\n" +
                "        \"valueAsAbsolute\": 396564.95871022344\n" +
                "      },\n" +
                "      \"eligibleData\": null\n" +
                "    },\n" +
                "    \"opex\": {\n" +
                "      \"totalAmount\": {\n" +
                "        \"quality\": \"Estimated\",\n" +
                "        \"dataSource\": null,\n" +
                "        \"comment\": null,\n" +
                "        \"value\": 940716.1856535822\n" +
                "      },\n" +
                "      \"alignedData\": null,\n" +
                "      \"eligibleData\": null\n" +
                "    },\n" +
                "    \"revenue\": {\n" +
                "      \"totalAmount\": {\n" +
                "        \"quality\": null,\n" +
                "        \"dataSource\": {\n" +
                "          \"report\": null,\n" +
                "          \"page\": null,\n" +
                "          \"tagName\": null\n" +
                "        },\n" +
                "        \"comment\": null,\n" +
                "        \"value\": 470269.4387175143\n" +
                "      },\n" +
                "      \"alignedData\": {\n" +
                "        \"valueAsPercentage\": 0.4347,\n" +
                "        \"quality\": \"Reported\",\n" +
                "        \"dataSource\": {\n" +
                "          \"report\": \"SustainabilityReport\",\n" +
                "          \"page\": 246,\n" +
                "          \"tagName\": \"communities\"\n" +
                "        },\n" +
                "        \"comment\": \"transmit online pixel\",\n" +
                "        \"valueAsAbsolute\": 840897.8063846007\n" +
                "      },\n" +
                "      \"eligibleData\": {\n" +
                "        \"valueAsPercentage\": null,\n" +
                "        \"quality\": \"Incomplete\",\n" +
                "        \"dataSource\": {},\n" +
                "        \"comment\": null,\n" +
                "        \"valueAsAbsolute\": 917185.3733132593\n" +
                "      }\n" +
                "    },\n" +
                "    \"fiscalYearDeviation\": \"Deviation\",\n" +
                "    \"fiscalYearEnd\": \"2022-10-05\",\n" +
                "    \"scopeOfEntities\": \"NA\",\n" +
                "    \"nfrdMandatory\": null,\n" +
                "    \"euTaxonomyActivityLevelReporting\": \"No\",\n" +
                "    \"assurance\": {\n" +
                "      \"assurance\": \"LimitedAssurance\",\n" +
                "      \"provider\": null,\n" +
                "      \"dataSource\": {\n" +
                "        \"report\": \"SustainabilityReport\",\n" +
                "        \"page\": 91,\n" +
                "        \"tagName\": \"initiatives\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"numberOfEmployees\": null,\n" +
                "    \"referencedReports\": {\n" +
                "      \"ESEFReport\": {\n" +
                "        \"reference\": \"50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63\",\n" +
                "        \"isGroupLevel\": null,\n" +
                "        \"reportDate\": null,\n" +
                "        \"currency\": \"BSD\"\n" +
                "      },\n" +
                "      \"SustainabilityReport\": {\n" +
                "        \"reference\": \"50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63\",\n" +
                "        \"isGroupLevel\": null,\n" +
                "        \"reportDate\": null,\n" +
                "        \"currency\": \"THB\"\n" +
                "      }\n" +
                "    }\n" +
                "  }",
        )
        return buildDatabaseEntry(dataObject, "eutaxonomy-non-financials")
    }

    private fun buildOldExpectedTransformedDatabaseEntry(): String {
        val dataObject = JSONObject(
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
                "\"totalAmount\": {\n" +
                "        \"value\": null,\n" +
                "        \"quality\": \"Audited\",\n" +
                "        \"dataSource\": {\n" +
                "          \"report\": \"SustainabilityReport\",\n" +
                "          \"page\": 1017,\n" +
                "          \"tagName\": \"platforms\"\n" +
                "        },\n" +
                "        \"comment\": \"quantify 1080p driver\"" +
                "      },\n" +
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
        )
        return buildDatabaseEntry(dataObject, "eutaxonomy-non-financials")
    }
}
