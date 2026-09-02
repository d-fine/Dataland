package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateAllDataPointTableEntities
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
        private const val FILE_REFERENCE_FIELD = "fileReference"
        private const val FILE_NAME_FIELD = "fileName"
        private const val PUBLICATION_DATE_FIELD = "publicationDate"
    }

    private val logger = LoggerFactory.getLogger("Migration V33")

    override fun migrate(context: Context?) {
        if (!tableExists(context)) return
        migrateAllDataPointTableEntities(context) { dataPointTableEntity -> migrateDataPointTableEntity(dataPointTableEntity) }
    }

    private fun tableExists(context: Context?): Boolean =
        context!!
            .connection.metaData
            .getTables(null, null, "data_point_items", null)
            .next()

    /**
     * Removes the inferable document fields from every "dataSource" object found within the data point.
     */
    fun migrateDataPointTableEntity(dataPointTableEntity: DataPointTableEntity) {
        removeInferableDocumentFields(dataPointTableEntity.dataPoint, dataPointTableEntity.dataPointId)
    }

    /**
     * Recursively traverses the given JSON value and removes the inferable document fields from every
     * "dataSource" object that references a document via "fileReference".
     */
    private fun removeInferableDocumentFields(
        jsonValue: Any?,
        dataPointId: String,
    ) {
        when (jsonValue) {
            is JSONObject -> {
                jsonValue.keys().asSequence().toList().forEach { key ->
                    val value = jsonValue.opt(key)
                    if (key == DATA_SOURCE_FIELD &&
                        value is JSONObject &&
                        (value.has(FILE_REFERENCE_FIELD) || value.has(FILE_NAME_FIELD))
                    ) {
                        removeFieldIfPresent(value, FILE_NAME_FIELD, dataPointId)
                        removeFieldIfPresent(value, PUBLICATION_DATE_FIELD, dataPointId)
                    } else {
                        removeInferableDocumentFields(value, dataPointId)
                    }
                }
            }
            is JSONArray -> {
                for (index in 0 until jsonValue.length()) {
                    removeInferableDocumentFields(jsonValue.opt(index), dataPointId)
                }
            }
            else -> {
                // Primitive values cannot contain nested data source nodes.
            }
        }
    }

    private fun removeFieldIfPresent(
        dataSource: JSONObject,
        field: String,
        dataPointId: String,
    ) {
        if (dataSource.has(field)) {
            logger.info("Removing field '$field' from dataSource of data point with id: $dataPointId")
            dataSource.remove(field)
        }
    }
}
