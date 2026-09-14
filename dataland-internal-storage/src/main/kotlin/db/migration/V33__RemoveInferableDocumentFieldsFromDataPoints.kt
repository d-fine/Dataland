package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script removes the "fileName" and "publicationDate" fields from every "dataSource" object
 * contained in a data point. These fields are inferable from the document referenced by "fileReference" and are
 * enriched on delivery from the document manager. They must no longer be persisted redundantly, as they can go
 * stale compared to the document manager, which is the single source of truth for document metadata. The
 * "fileReference" and "page" fields are the only non-inferable fields of a data source and are therefore
 * left untouched. The "tagName" field is also left untouched, as it has no counterpart in the document manager.
 */
@Suppress("ClassName")
class V33__RemoveInferableDocumentFieldsFromDataPoints : BaseJavaMigration() {
    companion object {
        private const val DATA_SOURCE_FIELD = "dataSource"
        private const val FILE_NAME_FIELD = "fileName"
        private const val PUBLICATION_DATE_FIELD = "publicationDate"

        private const val BATCH_SIZE = 1_000
        private const val PROGRESS_LOG_INTERVAL = 10_000L
    }

    private val logger = LoggerFactory.getLogger("Migration V33")
    private val objectMapper = ObjectMapper()

    override fun migrate(context: Context?) {
        if (context == null || !tableExists(context)) {
            logger.info("Table data_point_items does not exist. Skipping migration V33.")
            return
        }

        var lastDataPointId = ""
        var scannedRows = 0L
        var changedRows = 0L
        var removedFields = 0L

        while (true) {
            val batch = loadNextBatch(context, lastDataPointId)
            if (batch.isEmpty()) {
                break
            }

            lastDataPointId = batch.last().dataPointId
            scannedRows += batch.size

            val batchResult = migrateBatch(context, batch)
            changedRows += batchResult.changedRows
            removedFields += batchResult.removedFields

            if (scannedRows % PROGRESS_LOG_INTERVAL == 0L) {
                logger.info(
                    "Migration V33 progress: scanned {} candidate rows and changed {} rows.",
                    scannedRows,
                    changedRows,
                )
            }
        }

        logger.info(
            "Migration V33 completed: scanned {} candidate rows, changed {} rows, and removed {} fields.",
            scannedRows,
            changedRows,
            removedFields,
        )
    }

    /**
     * Applies the field removal to a batch of rows and writes back the ones that changed via a JDBC batch update.
     * @return the number of rows and fields that were changed within this batch
     */
    private fun migrateBatch(
        context: Context,
        batch: List<DataPointRow>,
    ): BatchResult {
        var changedRows = 0L
        var removedFields = 0L

        context.connection
            .prepareStatement(
                """
                UPDATE public.data_point_items
                SET data = ?
                WHERE data_point_id = ?
                """.trimIndent(),
            ).use { updateStatement ->
                var updatesInBatch = 0

                batch.forEach { row ->
                    val innerJson = objectMapper.readValue(row.rawData, String::class.java)
                    val dataPoint = JSONObject(innerJson)

                    val removedFromDataPoint = removeInferableDocumentFields(dataPoint)

                    if (removedFromDataPoint > 0) {
                        val storedData = objectMapper.writeValueAsString(dataPoint.toString())

                        updateStatement.setString(1, storedData)
                        updateStatement.setString(2, row.dataPointId)
                        updateStatement.addBatch()

                        changedRows++
                        removedFields += removedFromDataPoint
                        updatesInBatch++
                    }
                }

                if (updatesInBatch > 0) {
                    updateStatement.executeBatch()
                }
            }

        return BatchResult(changedRows, removedFields)
    }

    /**
     * Loads the next batch of candidate rows using keyset pagination on the primary key. Only rows that could
     * possibly contain "fileName" or "publicationDate" are returned.
     */
    private fun loadNextBatch(
        context: Context,
        lastDataPointId: String,
    ): List<DataPointRow> =
        context.connection
            .prepareStatement(
                """
                SELECT data_point_id, data
                FROM public.data_point_items
                WHERE data_point_id > ?
                  AND (
                        data LIKE '%fileName%'
                     OR data LIKE '%publicationDate%'
                  )
                ORDER BY data_point_id
                LIMIT ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, lastDataPointId)
                statement.setInt(2, BATCH_SIZE)

                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(
                                DataPointRow(
                                    dataPointId = resultSet.getString("data_point_id"),
                                    rawData = resultSet.getString("data"),
                                ),
                            )
                        }
                    }
                }
            }

    /**
     * Recursively traverses the given JSON value and removes the inferable document fields from every
     * "dataSource" object found within it.
     * @return the number of fields removed
     */
    private fun removeInferableDocumentFields(jsonValue: Any?): Int =
        when (jsonValue) {
            is JSONObject -> {
                var removedFields = 0
                jsonValue.keys().asSequence().toList().forEach { key ->
                    val value = jsonValue.opt(key)
                    if (key == DATA_SOURCE_FIELD && value is JSONObject) {
                        removedFields += removeFieldIfPresent(value, FILE_NAME_FIELD)
                        removedFields += removeFieldIfPresent(value, PUBLICATION_DATE_FIELD)
                    }
                    removedFields += removeInferableDocumentFields(value)
                }
                removedFields
            }

            is JSONArray -> {
                var removedFields = 0
                for (index in 0 until jsonValue.length()) {
                    removedFields += removeInferableDocumentFields(jsonValue.opt(index))
                }
                removedFields
            }

            else -> {
                0
            }
        }

    private fun removeFieldIfPresent(
        dataSource: JSONObject,
        field: String,
    ): Int {
        if (!dataSource.has(field)) {
            return 0
        }
        dataSource.remove(field)
        return 1
    }

    private fun tableExists(context: Context): Boolean =
        context.connection.metaData
            .getTables(null, null, "data_point_items", null)
            .use { resultSet -> resultSet.next() }

    private data class DataPointRow(
        val dataPointId: String,
        val rawData: String,
    )

    private data class BatchResult(
        val changedRows: Long,
        val removedFields: Long,
    )
}
