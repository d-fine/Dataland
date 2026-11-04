package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseMockedFlywayMigrationTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@Suppress("ClassName")
class V18__RemovePcafEstimationDataPointTypesTest : BaseMockedFlywayMigrationTest() {
    private val migration = V18__RemovePcafEstimationDataPointTypes()
    private val tableNames = listOf("data_point_meta_information", "data_point_uuid_map")

    @Test
    fun `check that migration does not start if tables are missing`() {
        stubTablesExist(tableNames, false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that estimated types are deleted in both tables when present`() {
        stubTablesExist(tableNames, true)
        stubPreparedStatement(1)

        migration.migrate(mockContext)

        verify(mockConnection, times(V18__RemovePcafEstimationDataPointTypes.deletedDataPointTypes.size * tableNames.size))
            .prepareStatement(any<String>())
    }
}
