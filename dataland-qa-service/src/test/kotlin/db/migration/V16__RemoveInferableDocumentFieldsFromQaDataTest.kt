package db.migration

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("ClassName")
class V16__RemoveInferableDocumentFieldsFromQaDataTest {
    private val migration = V16__RemoveInferableDocumentFieldsFromQaData()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    private val tables = listOf("data_point_qa_reports", "dataset_judgement_entity_data_point_judgement", "qa_reports")

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    private fun testMigrationOfSingleJson(
        locationOfOriginalJson: String,
        locationOfExpectedJson: String,
        migratingFunction: (JsonNode) -> JsonNode,
    ) {
        val originalJson = defaultObjectMapper.readTree(javaClass.getResource("/db/migration/$locationOfOriginalJson")!!.readText())
        val expectedJson = javaClass.getResource("/db/migration/$locationOfExpectedJson")!!.readText()

        val migratedJson = migratingFunction(originalJson)
        JSONAssert.assertEquals(expectedJson, defaultObjectMapper.writeValueAsString(migratedJson), true)
    }

    private fun mockSingleRowInDataPointQaReports(correctedData: String) {
        val existingTable = mock<ResultSet>()
        whenever(existingTable.next()).thenReturn(true)
        tables.forEach { whenever(mockMetaData.getTables(null, null, it, null)).thenReturn(mockResultSet) }
        whenever(mockMetaData.getTables(null, null, "data_point_qa_reports", null)).thenReturn(existingTable)

        val rows = mock<ResultSet>()
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeQuery()).thenReturn(rows)
        whenever(rows.next()).thenReturn(true, false)
        whenever(rows.getString("qa_report_id")).thenReturn("report-id")
        whenever(rows.getString("corrected_data")).thenReturn(correctedData)
    }

    @Test
    fun `check migration for data point`() {
        testMigrationOfSingleJson(
            "V16/originalDataPoint.json",
            "V16/expectedDataPoint.json",
            migration::cleanDataPoint,
        )
    }

    @Test
    fun `check migration for legacy QA report`() {
        testMigrationOfSingleJson(
            "V16/originalQaReport.json",
            "V16/expectedQaReport.json",
            migration::cleanQaReport,
        )
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        tables.forEach { whenever(mockMetaData.getTables(null, null, it, null)).thenReturn(mockResultSet) }
        whenever(mockResultSet.next()).thenReturn(false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that migration updates rows containing inferable fields`() {
        mockSingleRowInDataPointQaReports("""{"value":1,"dataSource":{"page":"8","fileName":"Annual Report"}}""")

        migration.migrate(mockContext)

        verify(mockPreparedStatement).setString(1, """{"value":1,"dataSource":{"page":"8"}}""")
        verify(mockPreparedStatement).setString(2, "report-id")
        verify(mockPreparedStatement).executeUpdate()
    }

    @Test
    fun `check that migration throws on invalid JSON`() {
        mockSingleRowInDataPointQaReports("{fileName:")

        val exception = assertThrows<IllegalStateException> { migration.migrate(mockContext) }

        assertEquals("Invalid JSON in data_point_qa_reports.corrected_data for ID report-id", exception.message)
    }
}
