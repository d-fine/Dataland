package db.migration.utils

import org.junit.jupiter.api.Assertions

class TestUtilsBackendMigration {
    private val dummyDataId = "abc"

    fun testMigrationOfDataPointIdAndDataPointTypeEntity(
        oldDataPointType: String,
        expectedDataPointType: String,
        migration: (input: DataPointIdAndDataPointTypeEntity) -> Unit,
    ) {
        val originalDataPointIdAndDataPointTypeEntity =
            DataPointIdAndDataPointTypeEntity(
                dummyDataId,
                oldDataPointType,
            )
        val expectedDataPointIdAndDataPointTypeEntity =
            DataPointIdAndDataPointTypeEntity(
                dummyDataId,
                expectedDataPointType,
            )
        migration(originalDataPointIdAndDataPointTypeEntity)
        Assertions.assertEquals(expectedDataPointIdAndDataPointTypeEntity, originalDataPointIdAndDataPointTypeEntity)
    }
}
