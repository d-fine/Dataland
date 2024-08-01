package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing EU taxonomy non-financials datasets and migrates all
 * existing BaseDataPoints to Extended ones.
 * Furthermore, some Points are moved from Wastes to Biodiversity
 */
class V19__MigrateEutaxonomyOnlyExtendedDatapoints : BaseJavaMigration() {
    /**
     * Move the required fields from wastes to biodiversity
     */
    private val relevantFieldNames = listOf(
        "scopeOfEntities",
        "nfrdMandatory",
        "euTaxonomyActivityLevelReporting",
    )

    private fun removeScopeOfEntities(dataset: JSONObject) {
        val general = dataset.getOrJavaNull("general") as JSONObject? ?: return
        val generalGeneral = general.getOrJavaNull("general") as JSONObject? ?: return
        if (generalGeneral.has("scopeOfEntities")) generalGeneral.remove("scopeOfEntities")
    }

    /**
     * Find all data points with a quality entry of NA and remove it
     */
    private fun checkRecursivelyForBaseDataPoint(
        dataset: JSONObject,
        objectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        println(objectName)
        if (obj !== null && obj is JSONObject) {
            println("${obj.names()}")
            /**
             var quality = null as String?
             if (obj.has("quality")) {
             quality = obj.getOrJavaNull("quality").toString()
             }
             if (quality == null || quality == "NA") {
             obj.remove("quality")
             }
             */
            obj.keys().forEach {
                checkRecursivelyForBaseDataPoint(obj, it)
            }
        }
    }

    /**
     * Move some fields and make all datapoints extended
     */
    public fun migrateEutaxonomyData(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            checkRecursivelyForBaseDataPoint(dataset, it)
        }
    }

    /**
     * Remove NA option from datapoints with quality
     */
    fun migrateDataPoints(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            checkRecursivelyForBaseDataPoint(dataset, it)
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials",
            this::migrateEutaxonomyData,
        )
    }
}
