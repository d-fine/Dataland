package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.TestUtils
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
        TestUtils().testMigrationOfSingleDataset(
            "V5/oldOriginalDatabaseEntry.json",
            "V5/oldExpectedTransformedDatabaseEntry.json",
            V5__MigrateToNewEuTaxonomyForNonFinancials()::migrateEuTaxonomyData
        )
        TestUtils().testMigrationOfSingleDataset(
            "V5/oldOriginalDatabaseEntry2.json",
            "V5/oldExpectedTransformedDatabaseEntry2.json",
            V5__MigrateToNewEuTaxonomyForNonFinancials()::migrateEuTaxonomyData
        )
    }
}
