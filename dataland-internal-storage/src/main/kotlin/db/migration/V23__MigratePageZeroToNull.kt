package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script updates all datasets where the page field is smaller or equal to 0,
 * setting the value to NULL. It also converts valid page integers into strings.
 */
class V23__MigratePageZeroToNull : BaseJavaMigration() {

    private val frameworksToMigrate = listOf(
        "eutaxonomy-non-financials",
        "eutaxonomy-financials",
        "sfdr",
        "additional-company-information",
    )

    private val logger = LoggerFactory.getLogger("Migration V23")

    override fun migrate(context: Context?) {
        frameworksToMigrate.forEach { framework ->
            migrateCompanyAssociatedDataOfDatatype(
                context,
                framework,
            ) { dataTableEntity -> migratePageFields(dataTableEntity) }
        }
    }

    /**
     * Migrates the page fields in the given `DataTableEntity` to match the new validation rules.
     * If a page field has a value less than or equal to 0, it sets the value to NULL.
     * Valid integer page values are converted to strings.
     *
     * @param dataTableEntity The entity containing the dataset to be migrated.
     */
    fun migratePageFields(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        fun updatePageFields(jsonObject: JSONObject) {
            jsonObject.keys().forEachRemaining { key ->
                val value = jsonObject.opt(key)

                when {
                    key == "page" && value is Number ->
                        when {
                            value.toLong() > 0 -> jsonObject.put(key, value.toString())
                            else -> {
                                logger.info(
                                    "Setting page field with value $value to NULL " +
                                        "in dataset ${dataTableEntity.dataId}",
                                )
                                jsonObject.put(key, JSONObject.NULL)
                            }
                        }
                    key == "page" && value != JSONObject.NULL -> {
                        logger.info("Page field has unexpected value '$value' in dataset ${dataTableEntity.dataId}")
                        jsonObject.put(key, JSONObject.NULL)
                    }
                    value is JSONObject -> {
                        updatePageFields(value)
                    }
                }
            }
        }

        updatePageFields(dataset)
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
