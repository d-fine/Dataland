package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseMockedFlywayMigrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.ResultSet

@Suppress("ClassName")
class V22__UnifySfdrAndPcafEnumDataPointTypesTest : BaseMockedFlywayMigrationTest() {
    private val migration = V22__UnifySfdrAndPcafEnumDataPointTypes()
    private val tableNames = V22__UnifySfdrAndPcafEnumDataPointTypes.tablesWithDataPointType
    private val renameMap = V22__UnifySfdrAndPcafEnumDataPointTypes.renameMap
    private val sfdrType = renameMap.keys.first()
    private val pcafType = renameMap.values.first()

    private fun renameSql(tableName: String) = "UPDATE $tableName SET data_point_type = ? WHERE data_point_type = ?"

    private fun deleteSql(tableName: String) = V22__UnifySfdrAndPcafEnumDataPointTypes.deleteCollidingPcafDataPointsSql(tableName)

    @Test
    fun `check that the SFDR enum data point types are mapped to the PCAF ones in all QA tables`() {
        assertEquals(
            mapOf(
                "extendedEnumSfdrMainPcafSector" to "extendedEnumPcafMainSector",
                "extendedEnumSfdrCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            ),
            renameMap,
        )
        assertEquals(
            listOf("data_point_qa_reports", "dataset_judgement_entity_data_point_judgement", "data_point_qa_review"),
            tableNames,
        )
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        stubTablesExist(tableNames, false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that the delete statement identifies colliding PCAF data points via the QA review table`() {
        val sql = deleteSql("data_point_qa_reports")

        assertTrue(sql.startsWith("DELETE FROM data_point_qa_reports WHERE data_point_type = ? AND data_point_id IN ("))
        assertTrue(sql.contains("FROM data_point_qa_review p WHERE p.data_point_type = ?"))
        assertTrue(sql.contains("FROM data_point_qa_review s WHERE s.data_point_type = ?"))
        assertTrue(sql.contains("s.company_id = p.company_id AND s.reporting_period = p.reporting_period"))
    }

    @Test
    fun `check that colliding PCAF data points are deleted with the correct parameters`() {
        stubPreparedStatement(1)

        migration.deleteCollidingPcafDataPoints(mockContext, "data_point_qa_review", sfdrType, pcafType)

        verify(mockConnection).prepareStatement(deleteSql("data_point_qa_review"))
        verify(mockPreparedStatement).setString(1, pcafType)
        verify(mockPreparedStatement).setString(2, pcafType)
        verify(mockPreparedStatement).setString(3, sfdrType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    @Test
    fun `check that data point type is renamed in selected QA table`() {
        stubPreparedStatement(2)

        migration.renameDataPointType(mockContext, "data_point_qa_reports", sfdrType, pcafType)

        verifyRenameStatementExecuted(renameSql("data_point_qa_reports"), sfdrType, pcafType)
    }

    @Test
    fun `check that colliding PCAF data points are deleted in all tables before renaming, review table last`() {
        stubTablesExist(tableNames, true)
        stubPreparedStatement(1)

        migration.migrate(mockContext)

        val inOrder = inOrder(mockConnection)
        repeat(renameMap.size) {
            tableNames.forEach { inOrder.verify(mockConnection).prepareStatement(deleteSql(it)) }
            tableNames.forEach { inOrder.verify(mockConnection).prepareStatement(renameSql(it)) }
        }
        verify(mockConnection, times(2 * tableNames.size * renameMap.size)).prepareStatement(any<String>())
    }

    @Test
    fun `check that no QA information is deleted but types are still renamed if the review table is missing`() {
        stubTablesExist(tableNames, true)
        val missingReviewTableResult = mock<ResultSet>()
        whenever(missingReviewTableResult.next()).thenReturn(false)
        whenever(mockMetaData.getTables(null, null, "data_point_qa_review", null)).thenReturn(missingReviewTableResult)
        stubPreparedStatement(1)

        migration.migrate(mockContext)

        tableNames.forEach { verify(mockConnection, never()).prepareStatement(deleteSql(it)) }
        verify(mockConnection, times(renameMap.size)).prepareStatement(renameSql("data_point_qa_reports"))
        verify(mockConnection, never()).prepareStatement(renameSql("data_point_qa_review"))
    }
}
