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
     * Find all data points with a value of NoEvidenceFound and move it to the quality bucket or with quality Incomplete
     * and move it to NoDataFound
     */
    private fun recursivelyChangeRelevantDataPointFields(
        dataset: JSONObject,
        objectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            if (obj.has("value") || obj.has("quality")) {
                val value = obj.getOrJavaNull("value").toString()
                val quality = obj.getOrJavaNull("quality").toString()
                if (value == "NoEvidenceFound") {
                    obj.put("value", JSONObject.NULL)
                    obj.put("quality", "NoDataFound")
                } else if (quality == "Incomplete") {
                    obj.put("quality", "NoDataFound")
                } else {
                    // Do nothing as no migration required in this case
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
