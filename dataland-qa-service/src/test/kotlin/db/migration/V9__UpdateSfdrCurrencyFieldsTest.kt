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
    private val context = mock<Context>()
    private val connection = mock<Connection>()
    private val statement = mock<Statement>()
    private val metaData = mock<DatabaseMetaData>()
    private val resultSet = mock<ResultSet>()
    private val preparedStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        whenever(context.connection).thenReturn(connection)
        whenever(connection.metaData).thenReturn(metaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        whenever(
            metaData.getTables(
                null, null,
                "data_point_qa_reports",
                null,
            ),
        ).thenReturn(resultSet)
        whenever(
            metaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(resultSet)
        whenever(resultSet.next()).thenReturn(false)

        migration.migrate(context)

        verify(connection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that currencyDeletion purges currency from JSON and updates Data Bank`() {
        val dataPointId = "dp123"
        val correctedDataJson = JSONObject(mapOf("currency" to "EUR", "value" to 100))

        val queueResultSet = mock<ResultSet>()
        whenever(connection.createStatement()).thenReturn(statement)
        whenever(statement.executeQuery(any<String>())).thenReturn(queueResultSet)

        whenever(queueResultSet.next()).thenReturn(true, false)
        whenever(queueResultSet.getString("data_point_id"))
            .thenReturn(dataPointId)
        whenever(queueResultSet.getString("corrected_data"))
            .thenReturn(correctedDataJson.toString())

        whenever(connection.prepareStatement(any<String>())).thenReturn(preparedStatement)

        migration.currencyDeletion(
            context, "data_point_qa_reports",
            "extendedCurrencyTotalRevenue",
        )

        verify(preparedStatement).setString(
            eq(1),
            eq(JSONObject(mapOf("value" to 100)).toString()),
        )
        verify(preparedStatement).setString(
            eq(2),
            eq(dataPointId),
        )
        verify(preparedStatement).executeUpdate()
        verify(preparedStatement).close()
        verify(queueResultSet).close()
    }

    @Test
    fun `sample check that migrateBackendTable updates extendedCurrencyEnterpriseValue correctly`() {
        whenever(connection.prepareStatement(any<String>())).thenReturn(preparedStatement)
        whenever(preparedStatement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context, "data_point_qa_review",
            "extendedCurrencyEnterpriseValue",
        )

        verify(preparedStatement).setString(1, "extendedDecimalEnterpriseValueInEUR")
        verify(preparedStatement).setString(2, "extendedCurrencyEnterpriseValue")
        verify(preparedStatement).executeUpdate()
        verify(preparedStatement).close()
    }

    @Test
    fun `check that migrateBackendTable throws on unknown dataPointType`() {
        assertThrows<IllegalArgumentException> {
            migration.migrateBackendTable(context, "some_table", "unknown_type")
        }
    }

    @Test
    fun `check that migrate executes all updates when tables exist`() {
        val reviewResultSet = mock<ResultSet>()
        val reportsResultSet = mock<ResultSet>()

        whenever(
            metaData.getTables(
                null,
                null,
                "data_point_qa_reports",
                null,
            ),
        ).thenReturn(reportsResultSet)
        whenever(
            metaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(reviewResultSet)
        whenever(reportsResultSet.next()).thenReturn(true)
        whenever(reviewResultSet.next()).thenReturn(true)

        whenever(connection.createStatement()).thenReturn(statement)
        val rs = mock<ResultSet>()
        whenever(statement.executeQuery(any<String>())).thenReturn(rs)
        whenever(rs.next()).thenReturn(false) // skip loop
        whenever(connection.prepareStatement(any<String>())).thenReturn(preparedStatement)
        whenever(preparedStatement.executeUpdate()).thenReturn(1)

        migration.migrate(context)

        verify(
            connection,
            times(9),
        ).prepareStatement(argThat { input -> input.startsWith("UPDATE") })
    }
}
