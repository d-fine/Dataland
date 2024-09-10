package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script updates all datasets where the page field smaller equal 0,
 * setting the value to NULL to match the new validation rules.
 */
class V23__MigratePageZeroToNull : BaseJavaMigration() {

    private val frameworksToMigrate = listOf(
        "eutaxonomy-non-financials",
        "eutaxonomy-financials",
        "sfdr",
    )

    private val logger = LoggerFactory.getLogger("Migration V23")

    override fun migrate(context: Context?) {
        frameworksToMigrate.forEach { framework ->
            migrateCompanyAssociatedDataOfDatatype(
                context,
                framework,
            ) { dataTableEntity -> migratePageFields(dataTableEntity, framework) }
        }
    }

    /**
     * Migrates the page fields in the given `DataTableEntity` to match the new validation rules.
     * If a page field has a value less than or equal to 0, it sets the value to NULL.
     *
     * @param dataTableEntity The entity containing the dataset to be migrated.
     * @param framework The framework name associated with the dataset.
     */
    fun migratePageFields(dataTableEntity: DataTableEntity, framework: String) {
        val dataset = dataTableEntity.dataJsonObject

        fun updatePageFields(jsonObject: JSONObject) {
            jsonObject.keys().forEachRemaining { key ->
                if (key == "page") {
                    val value = jsonObject.optInt(key, -1)
                    if (value <= 0) {
                        logger.info(
                            "Setting page with value $value to NULL for framework: $framework in dataset " +
                                dataTableEntity.dataId,
                        )
                        jsonObject.put(key, JSONObject.NULL)
                    }
                } else if (jsonObject.opt(key) is JSONObject) {
                    updatePageFields(jsonObject.getJSONObject(key))
                } else {
                    // do nothing
                }
            }
        }

        updatePageFields(dataset)
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
