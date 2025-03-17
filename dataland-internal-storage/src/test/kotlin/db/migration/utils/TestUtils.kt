package db.migration.utils

import org.junit.jupiter.api.Assertions

class TestUtils {
    private val mockDataId = "mock-data-id"

    fun testMigrationOfSingleDataset(
        dataType: String,
        oldDataFileLocation: String,
        migratedDataFileLocation: String,
        migration: (input: DataTableEntity) -> Unit,
    ) {
        val originalDataEntity =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile(oldDataFileLocation),
            )
        val expectedDataEntity =
            DataTableEntity.fromJsonObject(
                mockDataId,
                dataType,
                JsonUtils.readJsonFromResourcesFile(migratedDataFileLocation),
            )
        migration(originalDataEntity)
        Assertions.assertEquals(expectedDataEntity, originalDataEntity)
    }
}
