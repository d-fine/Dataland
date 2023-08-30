package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.JsonUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class V5__MigrateToNewEuTaxonomyForNonFinancialsTest {
    private val newEuTaxonomyForNonFinancials = "new-eutaxonomy-non-financials"
    private val euTaxonomyForNonFinancials = "eutaxonomy-non-financials"
    private val mockDataId = "mock-data-id"

    @Test
    fun `test that eu taxonomy for non financials migration script works as expected for migrating new data`() {
        val originalDataEntity = DataTableEntity.fromJsonObject(
            mockDataId, newEuTaxonomyForNonFinancials,
            JSONObject(
                "{" +
                    "\"something\": \"something\"" +
                    "}",
            ),
        )
        val expectedDataEntity = DataTableEntity.fromJsonObject(
            mockDataId, euTaxonomyForNonFinancials,
            JSONObject(
                "{" +
                    "\"something\": \"something\"" +
                    "}",
            ),
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrateNewEuTaxonomyData(originalDataEntity)
        Assertions.assertEquals(originalDataEntity, expectedDataEntity)
    }

    @Test
    fun `test that eu taxonomy for non financials migration script works as expected for migrating old data`() {
        testMigrationOfSingleDataset("oldOriginalDatabaseEntry.json", "oldExpectedTransformedDatabaseEntry.json")
        testMigrationOfSingleDataset("oldOriginalDatabaseEntry2.json", "oldExpectedTransformedDatabaseEntry2.json")
    }

    private fun testMigrationOfSingleDataset(filenameOfOldData: String, filenameOfMigratedData: String) {
        val originalDataEntity = DataTableEntity.fromJsonObject(
            mockDataId,
            euTaxonomyForNonFinancials,
            JsonUtils.readJsonFromResourcesFile("V5/$filenameOfOldData"),

        )
        val expectedDataEntity = DataTableEntity.fromJsonObject(
            mockDataId,
            euTaxonomyForNonFinancials,
            JsonUtils.readJsonFromResourcesFile("V5/$filenameOfMigratedData"),
        )
        val migration = V5__MigrateToNewEuTaxonomyForNonFinancials()
        migration.migrateEuTaxonomyData(originalDataEntity)
        Assertions.assertEquals(originalDataEntity, expectedDataEntity)
    }
}
