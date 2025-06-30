package db.migration

import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

@Suppress("ClassName")
class V9__UpdateSfdrCurrencyFieldsTest {
    private val migration = V9__UpdateSfdrCurrencyFields()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockStatement = mock<Statement>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockStatement, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        whenever(
            mockMetaData.getTables(
                null, null,
                "data_point_qa_reports",
                null,
            ),
        ).thenReturn(mockResultSet)
        whenever(
            mockMetaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that currencyDeletion purges currency from JSON and updates Data Bank`() {
        val dataPointId = "dp123"
        val correctedDataJson = JSONObject(mapOf("currency" to "EUR", "value" to 100))

        val queueResultSet = mock<ResultSet>()
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockStatement.executeQuery(any<String>())).thenReturn(queueResultSet)

        whenever(queueResultSet.next()).thenReturn(true, false)
        whenever(queueResultSet.getString("data_point_id"))
            .thenReturn(dataPointId)
        whenever(queueResultSet.getString("corrected_data"))
            .thenReturn(correctedDataJson.toString())

        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)

        migration.currencyDeletion(
            mockContext, "data_point_qa_reports",
            "extendedCurrencyTotalRevenue",
        )

        verify(mockPreparedStatement).setString(
            eq(1),
            eq(JSONObject(mapOf("value" to 100)).toString()),
        )
        verify(mockPreparedStatement).setString(
            eq(2),
            eq(dataPointId),
        )
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
        verify(queueResultSet).close()
    }

    @Test
    fun `sample check that migrateBackendTable updates extendedCurrencyEnterpriseValue correctly`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            mockContext, "data_point_qa_review",
            "extendedCurrencyEnterpriseValue",
        )

        verify(mockPreparedStatement).setString(1, "extendedDecimalEnterpriseValueInEUR")
        verify(mockPreparedStatement).setString(2, "extendedCurrencyEnterpriseValue")
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    @Test
    fun `check that migrateBackendTable throws on unknown dataPointType`() {
        assertThrows<IllegalArgumentException> {
            migration.migrateBackendTable(mockContext, "some_table", "unknown_type")
        }
    }

    @Test
    fun `check that migrate executes all updates when tables exist`() {
        val reviewResultSet = mock<ResultSet>()
        val reportsResultSet = mock<ResultSet>()

        whenever(
            mockMetaData.getTables(
                null,
                null,
                "data_point_qa_reports",
                null,
            ),
        ).thenReturn(reportsResultSet)
        whenever(
            mockMetaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(reviewResultSet)
        whenever(reportsResultSet.next()).thenReturn(true)
        whenever(reviewResultSet.next()).thenReturn(true)

        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        val rs = mock<ResultSet>()
        whenever(mockStatement.executeQuery(any<String>())).thenReturn(rs)
        whenever(rs.next()).thenReturn(false) // skip loop
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(1)

        migration.migrate(mockContext)

        verify(
            mockConnection,
            times(9),
        ).prepareStatement(argThat { input -> input.startsWith("UPDATE") })
    }
}
