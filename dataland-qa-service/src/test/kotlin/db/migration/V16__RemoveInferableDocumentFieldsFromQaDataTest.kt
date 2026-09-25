package db.migration

import org.dataland.datalandbackendutils.utils.JsonUtils
import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.sql.DriverManager
import java.util.UUID

@Suppress("ClassName")
class V16__RemoveInferableDocumentFieldsFromQaDataTest {
    @Test
    @Suppress("LongMethod")
    fun `cleans existing reports custom values and legacy reports without changing unrelated data`() {
        DriverManager.getConnection("jdbc:h2:mem:v16_qa_${UUID.randomUUID()};DATABASE_TO_LOWER=TRUE").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE data_point_qa_reports (qa_report_id VARCHAR(40) PRIMARY KEY, corrected_data CLOB)")
                statement.execute("CREATE TABLE dataset_judgement_entity_data_point_judgement (id UUID PRIMARY KEY, custom_value CLOB)")
                statement.execute("CREATE TABLE qa_reports (qa_report_id VARCHAR(40) PRIMARY KEY, qa_report CLOB)")
            }
            val dataPoint =
                """{"value":39,"dataSource":{"page":"8","fileReference":"ref","fileName":"report", """ +
                    """"publicationDate":"2026-07-21"},"other":[{"dataSource":{"fileName":null,"tagName":"tag"}}]}"""
            val cleaned =
                """{"value":39,"dataSource":{"page":"8","fileReference":"ref"},"other":[{"dataSource":{"tagName":"tag"}}]}"""
            val legacy = """{"energy":{"correctedData":$dataPoint,"comment":"fileName"}}"""
            val judgementId = UUID.randomUUID()
            connection.prepareStatement("INSERT INTO data_point_qa_reports VALUES (?, ?)").use { insert ->
                insert.setString(1, "report")
                insert.setString(2, dataPoint)
                insert.executeUpdate()
                insert.setString(1, "unchanged")
                insert.setString(2, """{"value":1,"comment":"fileName"}""")
                insert.executeUpdate()
                insert.setString(1, "null")
                insert.setString(2, null)
                insert.executeUpdate()
            }
            connection.prepareStatement("INSERT INTO dataset_judgement_entity_data_point_judgement VALUES (?, ?)").use { insert ->
                insert.setObject(1, judgementId)
                insert.setString(2, dataPoint)
                insert.executeUpdate()
            }
            connection.prepareStatement("INSERT INTO qa_reports VALUES (?, ?)").use { insert ->
                insert.setString(1, "legacy")
                insert.setString(2, legacy)
                insert.executeUpdate()
                insert.setString(1, "assembled")
                insert.setString(2, """["fileName"]""")
                insert.executeUpdate()
            }

            val context = mock<Context>()
            whenever(context.connection).thenReturn(connection)
            V16__RemoveInferableDocumentFieldsFromQaData().migrate(context)

            fun stored(
                table: String,
                column: String,
                idColumn: String,
                id: String,
            ): String =
                connection.prepareStatement("SELECT $column FROM $table WHERE $idColumn = ?").use { query ->
                    query.setString(1, id)
                    query.executeQuery().use { rows ->
                        rows.next()
                        rows.getString(1)
                    }
                }

            val mapper = JsonUtils.defaultObjectMapper
            assertEquals(
                mapper.readTree(cleaned),
                mapper
                    .readTree(stored("data_point_qa_reports", "corrected_data", "qa_report_id", "report")),
            )
            assertEquals(
                mapper.readTree(cleaned),
                mapper
                    .readTree(
                        stored("dataset_judgement_entity_data_point_judgement", "custom_value", "id", judgementId.toString()),
                    ),
            )
            assertEquals(
                mapper.readTree("""{"energy":{"correctedData":$cleaned,"comment":"fileName"}}"""),
                mapper.readTree(stored("qa_reports", "qa_report", "qa_report_id", "legacy")),
            )
            assertEquals(
                """{"value":1,"comment":"fileName"}""",
                stored("data_point_qa_reports", "corrected_data", "qa_report_id", "unchanged"),
            )
            assertEquals("""["fileName"]""", stored("qa_reports", "qa_report", "qa_report_id", "assembled"))
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT corrected_data FROM data_point_qa_reports WHERE qa_report_id = 'null'").use { rows ->
                    rows.next()
                    assertEquals(null, rows.getString(1))
                }
            }
        }
    }

    @Test
    fun `missing tables do not prevent migration of an existing table`() {
        DriverManager.getConnection("jdbc:h2:mem:v16_qa_${UUID.randomUUID()};DATABASE_TO_LOWER=TRUE").use { connection ->
            connection.createStatement().use {
                it
                    .execute("CREATE TABLE data_point_qa_reports (qa_report_id VARCHAR(40), corrected_data CLOB)")
            }
            connection.prepareStatement("INSERT INTO data_point_qa_reports VALUES (?, ?)").use { insert ->
                insert.setString(1, "report")
                insert.setString(2, """{"dataSource":{"fileName":"report"}}""")
                insert.executeUpdate()
            }
            val context = mock<Context>()
            whenever(context.connection).thenReturn(connection)
            V16__RemoveInferableDocumentFieldsFromQaData().migrate(context)
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT corrected_data FROM data_point_qa_reports").use { rows ->
                    rows.next()
                    assertEquals("""{"dataSource":{}}""", rows.getString(1))
                }
            }
        }
    }

    @Test
    fun `invalid candidate JSON fails with the row identifier`() {
        DriverManager.getConnection("jdbc:h2:mem:v16_qa_${UUID.randomUUID()};DATABASE_TO_LOWER=TRUE").use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE data_point_qa_reports (qa_report_id VARCHAR(40), corrected_data CLOB)")
                it.execute("INSERT INTO data_point_qa_reports VALUES ('broken', '{fileName:')")
            }
            val context = mock<Context>()
            whenever(context.connection).thenReturn(connection)
            val exception = assertThrows<IllegalStateException> { V16__RemoveInferableDocumentFieldsFromQaData().migrate(context) }
            assertEquals("Invalid JSON in data_point_qa_reports.corrected_data for ID broken", exception.message)
        }
    }
}
