package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

@Suppress("ClassName")
class V17__PinAcceptedQaReportTest {
    private val context = mock<Context>()
    private val connection = mock<Connection>()
    private val metaData = mock<DatabaseMetaData>()
    private val statement = mock<Statement>()

    @Test
    fun `skips the migration on fresh databases without judgement tables`() {
        whenever(context.connection).thenReturn(connection)
        whenever(connection.metaData).thenReturn(metaData)
        val tables = mock<ResultSet>()
        whenever(metaData.getTables(isNull(), isNull(), eq("dataset_judgement_entity_data_point_judgement"), isNull()))
            .thenReturn(tables)
        whenever(tables.next()).thenReturn(false)

        V17__PinAcceptedQaReport().migrate(context)

        verify(connection, never()).createStatement()
    }

    @Test
    fun `adds the pinned report column and resets pending legacy QA selections`() {
        whenever(context.connection).thenReturn(connection)
        whenever(connection.metaData).thenReturn(metaData)
        whenever(connection.createStatement()).thenReturn(statement)
        val tables = mock<ResultSet>()
        whenever(metaData.getTables(isNull(), isNull(), any<String>(), isNull())).thenReturn(tables)
        whenever(tables.next()).thenReturn(true)

        V17__PinAcceptedQaReport().migrate(context)

        val statements = argumentCaptor<String>()
        verify(statement, times(2)).execute(statements.capture())
        assertTrue(statements.allValues[0].contains("ADD COLUMN IF NOT EXISTS accepted_qa_report_id"))
        assertTrue(statements.allValues[1].contains("judgement.judgement_state = 'Pending'"))
        assertTrue(statements.allValues[1].contains("dp.accepted_source = 1"))
    }
}
