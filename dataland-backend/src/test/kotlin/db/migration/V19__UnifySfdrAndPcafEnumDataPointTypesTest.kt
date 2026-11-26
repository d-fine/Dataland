package db.migration

import org.dataland.datalandbackendutils.services.utils.BaseMockedFlywayMigrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@Suppress("ClassName")
class V19__UnifySfdrAndPcafEnumDataPointTypesTest : BaseMockedFlywayMigrationTest() {
    private val migration = V19__UnifySfdrAndPcafEnumDataPointTypes()
    private val tableNames =
        listOf(V19__UnifySfdrAndPcafEnumDataPointTypes.META_TABLE, V19__UnifySfdrAndPcafEnumDataPointTypes.UUID_TABLE)
    private val renameMap = V19__UnifySfdrAndPcafEnumDataPointTypes.renameMap
    private val deleteUuidSql = V19__UnifySfdrAndPcafEnumDataPointTypes.DELETE_COLLIDING_UUID_MAP_ENTRIES_SQL
    private val deleteMetaSql = V19__UnifySfdrAndPcafEnumDataPointTypes.DELETE_COLLIDING_META_INFORMATION_SQL
    private val renameMetaSql = "UPDATE data_point_meta_information SET data_point_type = ? WHERE data_point_type = ?"
    private val renameUuidSql = "UPDATE data_point_uuid_map SET data_point_identifier = ? WHERE data_point_identifier = ?"

    @Test
    fun `check that the SFDR enum data point types are mapped to the PCAF ones`() {
        assertEquals(
            mapOf(
                "extendedEnumSfdrMainPcafSector" to "extendedEnumPcafMainSector",
                "extendedEnumSfdrCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            ),
            renameMap,
        )
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        stubTablesExist(tableNames, false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that colliding PCAF data points are deleted with the PCAF type and the SFDR type as parameters`() {
        stubPreparedStatement(1)
        val (sfdrType, pcafType) = renameMap.entries.first().toPair()

        migration.deleteCollidingPcafDataPoints(mockContext, deleteMetaSql, sfdrType, pcafType)

        verify(mockConnection).prepareStatement(deleteMetaSql)
        verify(mockPreparedStatement).setString(1, pcafType)
        verify(mockPreparedStatement).setString(2, sfdrType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    @Test
    fun `check that data point type is renamed in selected table and column`() {
        stubPreparedStatement(3)
        val (sfdrType, pcafType) = renameMap.entries.first().toPair()

        migration.renameDataPointType(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            sourceType = sfdrType,
            targetType = pcafType,
        )

        verifyRenameStatementExecuted(renameMetaSql, sfdrType, pcafType)
    }

    @Test
    fun `check that the delete statements only target PCAF data points colliding with SFDR data points`() {
        listOf(deleteUuidSql, deleteMetaSql).forEach { sql ->
            assert(sql.contains("p.data_point_type = ?"))
            assert(sql.contains("s.data_point_type = ?"))
            assert(sql.contains("s.company_id = p.company_id"))
            assert(sql.contains("s.reporting_period = p.reporting_period"))
        }
        assert(deleteUuidSql.startsWith("DELETE FROM data_point_uuid_map WHERE data_point_uuid IN"))
        assert(deleteMetaSql.startsWith("DELETE FROM data_point_meta_information WHERE data_point_id IN"))
    }

    @Test
    fun `check that colliding PCAF data points are deleted before the SFDR data point types are renamed`() {
        stubTablesExist(tableNames, true)
        stubPreparedStatement(1)

        migration.migrate(mockContext)

        val inOrder = inOrder(mockConnection)
        repeat(renameMap.size) {
            inOrder.verify(mockConnection).prepareStatement(deleteUuidSql)
            inOrder.verify(mockConnection).prepareStatement(deleteMetaSql)
            inOrder.verify(mockConnection).prepareStatement(renameMetaSql)
            inOrder.verify(mockConnection).prepareStatement(renameUuidSql)
        }
        verify(mockConnection, times(4 * renameMap.size)).prepareStatement(any<String>())
        renameMap.forEach { (sfdrType, pcafType) ->
            verify(mockPreparedStatement, times(4)).setString(2, sfdrType)
            verify(mockPreparedStatement, times(4)).setString(1, pcafType)
        }
    }
}
