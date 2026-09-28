package db.migration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

/**
 * Removes the "fileName" and "publicationDate" fields from every "dataSource" object contained in the JSON
 * values stored in QA-related tables. These fields are inferable from the document referenced by
 * "fileReference" and are enriched on delivery from the document manager. They must no longer be persisted
 * redundantly, as they can go stale compared to the document manager, which is the single source of truth for
 * document metadata. The "fileReference" and "page" fields are the only non-inferable fields of a data source
 * and are therefore left untouched. The "tagName" field is also left untouched, as it has no counterpart in
 * the document manager.
 *
 * This applies to both directly stored data points (e.g. "corrected_data" and "custom_value") and to legacy
 * QA reports (the "qa_report" column), where a "dataSource" object can only ever occur nested inside a
 * "correctedData" subtree - so the same generic traversal handles all cases uniformly.
 */
@Suppress("ClassName")
class V16__RemoveInferableDocumentFieldsFromQaData : BaseJavaMigration() {
    private companion object {
        const val FETCH_SIZE = 500

        const val DATA_SOURCE_FIELD = "dataSource"
        const val FILE_NAME_FIELD = "fileName"
        const val PUBLICATION_DATE_FIELD = "publicationDate"
    }

    private data class TableMigrationTarget(
        val table: String,
        val idColumn: String,
        val valueColumn: String,
        val bindId: (PreparedStatement, Int, String) -> Unit,
    )

    private val targets =
        listOf(
            TableMigrationTarget(
                table = "data_point_qa_reports",
                idColumn = "qa_report_id",
                valueColumn = "corrected_data",
                bindId = { statement, index, id -> statement.setString(index, id) },
            ),
            TableMigrationTarget(
                table = "dataset_judgement_entity_data_point_judgement",
                idColumn = "id",
                valueColumn = "custom_value",
                bindId = { statement, index, id -> statement.setObject(index, UUID.fromString(id)) },
            ),
            TableMigrationTarget(
                table = "qa_reports",
                idColumn = "qa_report_id",
                valueColumn = "qa_report",
                bindId = { statement, index, id -> statement.setString(index, id) },
            ),
        )

    override fun migrate(context: Context?) {
        val connection = requireNotNull(context).connection
        targets.forEach { migrateTable(connection, it) }
    }

    private fun migrateTable(
        connection: Connection,
        target: TableMigrationTarget,
    ) {
        if (!tableExists(connection, target.table)) return
        migrateRows(connection, target)
    }

    private fun tableExists(
        connection: Connection,
        table: String,
    ): Boolean = connection.metaData.getTables(null, null, table, null).use { tables -> tables.next() }

    private fun migrateRows(
        connection: Connection,
        target: TableMigrationTarget,
    ) {
        connection
            .prepareStatement(
                "UPDATE ${target.table} SET ${target.valueColumn} = ? WHERE ${target.idColumn} = ?",
            ).use { update ->
                migrateSelectedRows(connection, target, update)
            }
    }

    private fun migrateSelectedRows(
        connection: Connection,
        target: TableMigrationTarget,
        update: PreparedStatement,
    ) {
        connection
            .prepareStatement(
                "SELECT ${target.idColumn}, ${target.valueColumn} FROM ${target.table} " +
                    "WHERE ${target.valueColumn} LIKE '%$FILE_NAME_FIELD%' OR ${target.valueColumn} LIKE '%$PUBLICATION_DATE_FIELD%'",
            ).use { select ->
                select.fetchSize = FETCH_SIZE
                select.executeQuery().use { rows ->
                    while (rows.next()) {
                        migrateRow(rows, update, target)
                    }
                }
            }
    }

    private fun migrateRow(
        rows: ResultSet,
        update: PreparedStatement,
        target: TableMigrationTarget,
    ) {
        val id = rows.getString(target.idColumn)
        val rawValue = rows.getString(target.valueColumn) ?: return
        val root =
            try {
                JsonUtils.defaultObjectMapper.readTree(rawValue)
            } catch (exception: JsonProcessingException) {
                throw IllegalStateException(
                    "Invalid JSON in ${target.table}.${target.valueColumn} for ID $id",
                    exception,
                )
            }

        if (removeInferableDocumentFields(root) == 0) return

        update.setString(1, JsonUtils.defaultObjectMapper.writeValueAsString(root))
        target.bindId(update, 2, id)
        update.executeUpdate()
    }

    /**
     * Recursively traverses the given JSON node and removes the inferable document fields from every
     * "dataSource" object found within it.
     * @return the number of fields removed
     */
    private fun removeInferableDocumentFields(node: JsonNode): Int =
        when {
            node.isArray -> node.sumOf { removeInferableDocumentFields(it) }
            node.isObject ->
                node.fieldNames().asSequence().toList().sumOf { key ->
                    val value = node.get(key)
                    var removed = 0
                    if (key == DATA_SOURCE_FIELD && value is ObjectNode) {
                        removed += removeFieldIfPresent(value, FILE_NAME_FIELD)
                        removed += removeFieldIfPresent(value, PUBLICATION_DATE_FIELD)
                    }
                    removed + removeInferableDocumentFields(value)
                }
            else -> 0
        }

    private fun removeFieldIfPresent(
        dataSource: ObjectNode,
        field: String,
    ): Int {
        if (!dataSource.has(field)) return 0
        dataSource.remove(field)
        return 1
    }
}
