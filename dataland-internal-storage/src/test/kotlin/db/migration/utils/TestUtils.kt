package db.migration.utils

import org.junit.jupiter.api.Assertions

class TestUtils {
    private val euTaxonomyForNonFinancials = "eutaxonomy-non-financials"
    private val mockDataId = "mock-data-id"

    fun testMigrationOfSingleDataset(
        oldDataFileLocation: String,
        migratedDataFileLocation: String,
        migration: (input: DataTableEntity) -> Unit,
    ) {
        val originalDataEntity = DataTableEntity.fromJsonObject(
            mockDataId,
            euTaxonomyForNonFinancials,
            JsonUtils.readJsonFromResourcesFile(oldDataFileLocation),

        )
        val expectedDataEntity = DataTableEntity.fromJsonObject(
            mockDataId,
            euTaxonomyForNonFinancials,
            JsonUtils.readJsonFromResourcesFile(migratedDataFileLocation),
        )
        migration(originalDataEntity)
        Assertions.assertEquals(originalDataEntity, expectedDataEntity)
    }
}
