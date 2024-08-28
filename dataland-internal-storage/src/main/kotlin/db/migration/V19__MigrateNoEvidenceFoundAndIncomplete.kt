package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script moves all SFDR Incomplete quality buckets to NoDataFound and removes the value NoEvidenceFound
 * by putting NoDataFound into the quality bucket and setting the value to null
 */
class V19__MigrateNoEvidenceFoundAndIncomplete : BaseJavaMigration() {

    /**
     * Apply the following two migrations:
     * 1. If value of the data point is "NoEvidenceFound", set value to null and the quality bucket to "NoDataFound"
     * 2. If the quality bucket of the data point is "Incomplete" set it to "NoDataFound" instead
     */
    private fun recursivelyChangeRelevantDataPointFields(
        jsonObject: JSONObject,
        objectName: String,
    ) {
        val obj = jsonObject.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            val isExtendedDataPoint = obj.has("value") && obj.has("quality")
            if (isExtendedDataPoint) {
                val value = obj.getOrJavaNull("value").toString()
                if (value == "NoEvidenceFound") {
                    obj.put("value", JSONObject.NULL)
                    obj.put("quality", "NoDataFound")
                }
            } else {
                obj.keys().forEach {
                    recursivelyChangeRelevantDataPointFields(obj, it)
                }
            }
        }
    }

    /**
     * Execute migration and update data sets accordingly
     */
    fun migrateSfdrData(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            recursivelyChangeRelevantDataPointFields(dataset, it)
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateSfdrData,
        )
    }
}
