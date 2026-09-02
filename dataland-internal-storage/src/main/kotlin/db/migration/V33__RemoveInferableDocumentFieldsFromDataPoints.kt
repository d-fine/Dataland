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
 *
 * Most stored data points are simple leaf objects with a single top-level "dataSource" (e.g. an
 * ExtendedDataPoint), but some data point types (e.g. "plainSfdrHighImpactClimateSectors", whose value is a
 * Map<HighImpactClimateSector, SfdrHighImpactClimateSectorEnergyConsumption>) are stored as a single data point
 * that embeds several independent "dataSource" objects nested within object keys. The traversal below therefore
 * recurses into nested objects to cover this case. Recursing into arrays is additionally handled defensively for
 * forward-compatibility, even though no data point type is currently known to place a "dataSource" inside a
 * JSON array.
 */
@Suppress("ClassName")
class V33__RemoveInferableDocumentFieldsFromDataPoints : BaseJavaMigration() {
    companion object {
        private const val DATA_SOURCE_FIELD = "dataSource"
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
     * "dataSource" object".
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
                        (value.has(FILE_NAME_FIELD) || value.has(PUBLICATION_DATE_FIELD))
                    ) {
                        removeFieldIfPresent(value, FILE_NAME_FIELD)
                        removeFieldIfPresent(value, PUBLICATION_DATE_FIELD)
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
                // Do nothing for primitive values (String, Number, Boolean, null)
            }
        }
    }

    private fun removeFieldIfPresent(
        dataSource: JSONObject,
        field: String,
    ) {
        if (dataSource.has(field)) {
            dataSource.remove(field)
        }
    }
}
