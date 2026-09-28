package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
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

    private fun mockSingleRow(
        table: String,
        idColumn: String,
        valueColumn: String,
        id: String,
        value: String?,
    ) {
        val existingTable = mock<ResultSet>()
        whenever(existingTable.next()).thenReturn(true)
        tables.forEach { whenever(mockMetaData.getTables(null, null, it, null)).thenReturn(mockResultSet) }
        whenever(mockMetaData.getTables(null, null, table, null)).thenReturn(existingTable)

        val rows = mock<ResultSet>()
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeQuery()).thenReturn(rows)
        whenever(rows.next()).thenReturn(true, false)
        whenever(rows.getString(idColumn)).thenReturn(id)
        whenever(rows.getString(valueColumn)).thenReturn(value)
    }

    private fun assertMigratedJsonMatchesFixture(
        table: String,
        idColumn: String,
        valueColumn: String,
        originalResource: String,
        expectedResource: String,
    ) {
        val originalJson = javaClass.getResource("/db/migration/$originalResource")!!.readText()
        val expectedJson = javaClass.getResource("/db/migration/$expectedResource")!!.readText()
        mockSingleRow(table, idColumn, valueColumn, "report-id", originalJson)

        migration.migrate(mockContext)

        val capturedValue = argumentCaptor<String>()
        verify(mockPreparedStatement).setString(eq(1), capturedValue.capture())
        JSONAssert.assertEquals(expectedJson, capturedValue.firstValue, true)
    }

    @Test
    fun `check migration for data point`() {
        assertMigratedJsonMatchesFixture(
            table = "data_point_qa_reports",
            idColumn = "qa_report_id",
            valueColumn = "corrected_data",
            originalResource = "V16/originalDataPoint.json",
            expectedResource = "V16/expectedDataPoint.json",
        )
    }

    @Test
    fun `check migration for legacy QA report`() {
        assertMigratedJsonMatchesFixture(
            table = "qa_reports",
            idColumn = "qa_report_id",
            valueColumn = "qa_report",
            originalResource = "V16/originalQaReport.json",
            expectedResource = "V16/expectedQaReport.json",
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
        mockSingleRow(
            table = "data_point_qa_reports",
            idColumn = "qa_report_id",
            valueColumn = "corrected_data",
            id = "report-id",
            value = """{"value":1,"dataSource":{"page":"8","fileName":"Annual Report"}}""",
        )

        migration.migrate(mockContext)

        verify(mockPreparedStatement).setString(1, """{"value":1,"dataSource":{"page":"8"}}""")
        verify(mockPreparedStatement).setString(2, "report-id")
        verify(mockPreparedStatement).executeUpdate()
    }

    @Test
    fun `check that migration throws on invalid JSON`() {
        mockSingleRow(
            table = "data_point_qa_reports",
            idColumn = "qa_report_id",
            valueColumn = "corrected_data",
            id = "report-id",
            value = "{fileName:",
        )

        val exception = assertThrows<IllegalStateException> { migration.migrate(mockContext) }

        assertEquals("Invalid JSON in data_point_qa_reports.corrected_data for ID report-id", exception.message)
    }
}
