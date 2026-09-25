package db.migration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

/** Removes document metadata that the backend now infers rather than accepting on upload. */
@Suppress("ClassName")
class V16__RemoveInferableDocumentFieldsFromQaData : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val FETCH_SIZE = 500
    }

    override fun migrate(context: Context?) {
        val connection = requireNotNull(context).connection
        migrateColumn(connection, "data_point_qa_reports", "qa_report_id", "corrected_data")
        migrateColumn(connection, "dataset_judgement_entity_data_point_judgement", "id", "custom_value")
        migrateColumn(connection, "qa_reports", "qa_report_id", "qa_report", legacy = true)
    }

    private fun migrateColumn(
        connection: Connection,
        table: String,
        idColumn: String,
        valueColumn: String,
        legacy: Boolean = false,
    ) {
        connection.metaData.getTables(null, null, table, null).use { tables ->
            if (!tables.next()) return
        }

        val changed = migrateRows(connection, table, idColumn, valueColumn, legacy)
        logger.info("Removed inferable document fields from $changed rows in $table.$valueColumn")
    }

    private fun migrateRows(
        connection: Connection,
        table: String,
        idColumn: String,
        valueColumn: String,
        legacy: Boolean,
    ): Int =
        connection
            .prepareStatement(
                "SELECT $idColumn, $valueColumn FROM $table WHERE $valueColumn LIKE '%fileName%' OR $valueColumn LIKE '%publicationDate%'",
            ).use { select ->
                select.fetchSize = FETCH_SIZE
                select.executeQuery().use { rows ->
                    connection.prepareStatement("UPDATE $table SET $valueColumn = ? WHERE $idColumn = ?").use { update ->
                        migrateSelectedRows(rows, update, table, idColumn, valueColumn, legacy)
                    }
                }
            }

    private fun migrateSelectedRows(
        rows: ResultSet,
        update: PreparedStatement,
        table: String,
        idColumn: String,
        valueColumn: String,
        legacy: Boolean,
    ): Int {
        var changed = 0
        while (rows.next()) {
            if (migrateRow(rows, update, table, idColumn, valueColumn, legacy)) changed++
        }
        return changed
    }

    private fun migrateRow(
        rows: ResultSet,
        update: PreparedStatement,
        table: String,
        idColumn: String,
        valueColumn: String,
        legacy: Boolean,
    ): Boolean {
        val id = rows.getString(idColumn)
        val root =
            try {
                JsonUtils.defaultObjectMapper.readTree(rows.getString(valueColumn))
            } catch (exception: JsonProcessingException) {
                throw IllegalStateException("Invalid JSON in $table.$valueColumn for ID $id", exception)
            }
        val removed =
            if (legacy && root.isObject) {
                removeFromLegacyReport(root)
            } else if (legacy) {
                0
            } else {
                removeFromDataPoint(root)
            }
        if (removed == 0) return false

        update.setString(1, JsonUtils.defaultObjectMapper.writeValueAsString(root))
        if (idColumn == "id") update.setObject(2, UUID.fromString(id)) else update.setString(2, id)
        update.executeUpdate()
        return true
    }

    private fun removeFromLegacyReport(node: JsonNode): Int =
        when {
            node.isArray -> node.sumOf { removeFromLegacyReport(it) }
            node.isObject ->
                node.fieldNames().asSequence().toList().sumOf { key ->
                    val value = node.get(key)
                    if (key == "correctedData") removeFromDataPoint(value) else removeFromLegacyReport(value)
                }
            else -> 0
        }

    private fun removeFromDataPoint(node: JsonNode): Int =
        when {
            node.isArray -> node.sumOf { removeFromDataPoint(it) }
            node.isObject ->
                node.fieldNames().asSequence().toList().sumOf { key ->
                    val value = node.get(key)
                    var removed = 0
                    if (key == "dataSource" && value is ObjectNode) {
                        if (value.has("fileName")) {
                            value.remove("fileName")
                            removed++
                        }
                        if (value.has("publicationDate")) {
                            value.remove("publicationDate")
                            removed++
                        }
                    }
                    removed + removeFromDataPoint(value)
                }
            else -> 0
        }
}
