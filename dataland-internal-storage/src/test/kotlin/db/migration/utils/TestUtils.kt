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

    fun testMigrationOfSingleDatapoint(
        oldDataPointType: String,
        expectedDataPointType: String,
        oldDataFileLocation: String,
        migratedDataFileLocation: String,
        migration: (input: DataPointTableEntity) -> Unit,
    ) {
        val originalDataPointEntity =
            DataPointTableEntity(
                mockDataId,
                JsonUtils.readJsonFromResourcesFile(oldDataFileLocation),
                oldDataPointType,
                reportingPeriod = "2023",
            )
        val expectedDataEntity =
            DataPointTableEntity(
                mockDataId,
                JsonUtils.readJsonFromResourcesFile(migratedDataFileLocation),
                expectedDataPointType,
                reportingPeriod = "2023",
            )
        migration(originalDataPointEntity)
        Assertions.assertEquals(expectedDataEntity, originalDataPointEntity)
    }
}
