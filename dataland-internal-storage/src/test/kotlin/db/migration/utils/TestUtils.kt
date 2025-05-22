package db.migration.utils

import org.junit.jupiter.api.Assertions

class TestUtils {
    private val mockDataId = "mock-data-id"
    private val dummyCompanyId = "123"
    private val dummyReportingPeriod = "2023"

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
                companyId = dummyCompanyId,
                JsonUtils.readJsonFromResourcesFile(oldDataFileLocation),
                oldDataPointType,
                reportingPeriod = dummyReportingPeriod,
            )
        val expectedDataEntity =
            DataPointTableEntity(
                mockDataId,
                companyId = dummyCompanyId,
                JsonUtils.readJsonFromResourcesFile(migratedDataFileLocation),
                expectedDataPointType,
                reportingPeriod = dummyReportingPeriod,
            )
        migration(originalDataPointEntity)
        Assertions.assertEquals(expectedDataEntity, originalDataPointEntity)
    }
}
