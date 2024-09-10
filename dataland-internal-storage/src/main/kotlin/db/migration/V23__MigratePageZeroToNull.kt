package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates all SFDR datasets where the page field is 0 or invalid,
 * setting the value to NULL to match the new validation rules.
 */
class V23__MigratePageZeroToNull : BaseJavaMigration() {

    private val regexPage = """^([1-9]\d*)(?:-([1-9]\d*))?$""".toRegex()

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migratePageFields,
        )
    }

    /**
     * Migrates SFDR data by setting 'page' values of 0 or invalid formats to NULL.
     */
    fun migratePageFields(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        // Recursive function to update page fields within the dataset
        fun updatePageFields(jsonObject: JSONObject) {
            jsonObject.keys().forEachRemaining { key ->
                val value = jsonObject.opt(key)

                if (value is JSONObject) {
                    updatePageFields(value)
                } else if (key == "page" && value is String) {
                    if (!regexPage.matches(value)) {
                        jsonObject.put(key, JSONObject.NULL) // Set invalid page to NULL
                    }
                } else if (key == "page" && value is Int) {
                    if (value <= 0) {
                        jsonObject.put(key, JSONObject.NULL) // Set page values <= 0 to NULL
                    } else {
                        jsonObject.put(key, value.toString()) // Convert valid page number to string
                    }
                }
            }
        }

        // Start processing the dataset
        updatePageFields(dataset)

        // Update the data field in the company associated data
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
