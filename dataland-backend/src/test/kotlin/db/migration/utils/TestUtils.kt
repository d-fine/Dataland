package db.migration.utils

import org.junit.jupiter.api.Assertions

class TestUtils {
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
