package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseMockedFlywayMigrationTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@Suppress("ClassName")
class V16__RenameAndDeleteSfdrDataPointTypesTest : BaseMockedFlywayMigrationTest() {
    private val migration = V16__RenameAndDeleteSfdrDataPointTypes()
    private val tableNames = listOf("data_point_meta_information", "data_point_uuid_map")

    private val sourceType = V16__RenameAndDeleteSfdrDataPointTypes.renameMap.keys.first()
    private val targetType = V16__RenameAndDeleteSfdrDataPointTypes.renameMap.values.first()
    private val deletedType = V16__RenameAndDeleteSfdrDataPointTypes.deletedDataPointTypes.first()

    @Test
    fun `check that migration does not start if tables are missing`() {
        stubTablesExist(tableNames, false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that data point type is renamed in selected table and column`() {
        stubPreparedStatement(3)

        migration.renameDataPointType(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            sourceType = sourceType,
            targetType = targetType,
        )

        verifyRenameStatementExecuted(
            "UPDATE data_point_meta_information SET data_point_type = ? WHERE data_point_type = ?",
            sourceType,
            targetType,
        )
    }

    @Test
    fun `check that data point type is deleted in selected table and column`() {
        stubPreparedStatement(2)

        migration.deleteDataPointType(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = deletedType,
        )

        verifyDeleteStatementExecuted(
            "DELETE FROM data_point_meta_information WHERE data_point_type = ?",
            deletedType,
        )
    }

    @Test
    fun `check that migration renames and deletes in both tables when present`() {
        stubTablesExist(tableNames, true)
        stubPreparedStatement(1)

        migration.migrate(mockContext)

        val statementsPerTable =
            V16__RenameAndDeleteSfdrDataPointTypes.renameMap.size +
                V16__RenameAndDeleteSfdrDataPointTypes.deletedDataPointTypes.size
        verify(mockConnection, times(statementsPerTable * tableNames.size)).prepareStatement(any<String>())
    }
}
