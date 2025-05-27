package db.migration

import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.startsWith
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

@Suppress("ClassName")
class V9__UpdateSfdrCurrencyFieldsTest {
    private lateinit var migration: V9__UpdateSfdrCurrencyFields
    private lateinit var context: Context
    private lateinit var connection: Connection
    private lateinit var statement: Statement
    private lateinit var metaData: DatabaseMetaData
    private lateinit var resultSet: ResultSet
    private lateinit var preparedStatement: PreparedStatement

    @BeforeEach
    fun setup() {
        migration = V9__UpdateSfdrCurrencyFields()
        context = mock(Context::class.java)
        connection = mock(Connection::class.java)
        statement = mock(Statement::class.java)
        metaData = mock(DatabaseMetaData::class.java)
        resultSet = mock(ResultSet::class.java)
        preparedStatement = mock(PreparedStatement::class.java)

        `when`(context.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(metaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        `when`(
            metaData.getTables(
                null, null,
                "data_point_qa_reports",
                null,
            ),
        ).thenReturn(resultSet)
        `when`(
            metaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(resultSet)
        `when`(resultSet.next()).thenReturn(false)

        migration.migrate(context)

        verify(connection, never()).prepareStatement(anyString())
    }

    @Test
    fun `check that currencyDeletion purges currency from JSON and updates Data Bank`() {
        val dataPointId = "dp123"
        val correctedDataJson = JSONObject(mapOf("currency" to "EUR", "value" to 100))

        val queueResultSet = mock(ResultSet::class.java)
        `when`(connection.createStatement()).thenReturn(statement)
        `when`(statement.executeQuery(anyString())).thenReturn(queueResultSet)

        `when`(queueResultSet.next()).thenReturn(true, false)
        `when`(queueResultSet.getString("data_point_id"))
            .thenReturn(dataPointId)
        `when`(queueResultSet.getString("corrected_data"))
            .thenReturn(correctedDataJson.toString())

        `when`(connection.prepareStatement(anyString())).thenReturn(preparedStatement)

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
        `when`(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
        `when`(preparedStatement.executeUpdate()).thenReturn(2)

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
        val reviewResultSet = mock(ResultSet::class.java)
        val reportsResultSet = mock(ResultSet::class.java)

        `when`(
            metaData.getTables(
                null,
                null,
                "data_point_qa_reports",
                null,
            ),
        ).thenReturn(reportsResultSet)
        `when`(
            metaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(reviewResultSet)
        `when`(reportsResultSet.next()).thenReturn(true)
        `when`(reviewResultSet.next()).thenReturn(true)

        `when`(connection.createStatement()).thenReturn(statement)
        val rs = mock(ResultSet::class.java)
        `when`(statement.executeQuery(anyString())).thenReturn(rs)
        `when`(rs.next()).thenReturn(false) // skip loop
        `when`(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
        `when`(preparedStatement.executeUpdate()).thenReturn(1)

        migration.migrate(context)

        verify(
            connection,
            times(9),
        ).prepareStatement(startsWith("UPDATE"))
    }
}
