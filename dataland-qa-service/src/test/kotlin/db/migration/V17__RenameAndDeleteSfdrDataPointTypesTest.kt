package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseMockedFlywayMigrationTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@Suppress("ClassName")
class V17__RenameAndDeleteSfdrDataPointTypesTest : BaseMockedFlywayMigrationTest() {
    private val migration = V17__RenameAndDeleteSfdrDataPointTypes()
    private val tableNames = V17__RenameAndDeleteSfdrDataPointTypes.tablesWithDataPointType

    private val sourceType = V17__RenameAndDeleteSfdrDataPointTypes.renameMap.keys.first()
    private val targetType = V17__RenameAndDeleteSfdrDataPointTypes.renameMap.values.first()
    private val deletedType = V17__RenameAndDeleteSfdrDataPointTypes.deletedDataPointTypes.first()

    @Test
    fun `check that migration does not start if tables are missing`() {
        stubTablesExist(tableNames, false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that data point type is renamed in selected QA table`() {
        stubPreparedStatement(2)

        migration.renameDataPointType(
            context = mockContext,
            tableName = "data_point_qa_review",
            sourceType = sourceType,
            targetType = targetType,
        )

        verifyRenameStatementExecuted(
            "UPDATE data_point_qa_review SET data_point_type = ? WHERE data_point_type = ?",
            sourceType,
            targetType,
        )
    }

    @Test
    fun `check that data point type is deleted in selected QA table`() {
        stubPreparedStatement(1)

        migration.deleteDataPointType(
            context = mockContext,
            tableName = "data_point_qa_review",
            dataPointType = deletedType,
        )

        verifyDeleteStatementExecuted(
            "DELETE FROM data_point_qa_review WHERE data_point_type = ?",
            deletedType,
        )
    }

    @Test
    fun `check that migration renames and deletes in all tables when present`() {
        stubTablesExist(tableNames, true)
        stubPreparedStatement(1)

        migration.migrate(mockContext)

        val statementsPerTable =
            V17__RenameAndDeleteSfdrDataPointTypes.renameMap.size +
                V17__RenameAndDeleteSfdrDataPointTypes.deletedDataPointTypes.size
        verify(mockConnection, times(statementsPerTable * tableNames.size)).prepareStatement(any<String>())
    }
}
