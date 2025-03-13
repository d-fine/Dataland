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

    /**
     * Read data sets including companyId and reporting period and return DataTableEntities as if they
     * were read from the database.
     *
     * @param fileLocations a list of files to read from
     */
    fun readDataSetsAsStoredInDatabase(fileLocations: List<String>): List<DataTableEntity> =
        fileLocations.map {
            val jsonData = JsonUtils.readJsonFromResourcesFile(it)
            DataTableEntity(
                mockDataId,
                jsonData.put("data", jsonData.getJSONObject("data").toString()),
            )
        }
}
