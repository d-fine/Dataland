package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing SFDR datasets and migrates all existing BaseDataPoints to Extended ones
 * Furthermore, some Points are moved from Wastes to Biodiversity
 */
class V16__MigrateSfdrMinorChangesOnlyExtendedDatapoints : BaseJavaMigration() {

    private val listOfWasteToBiodiversity = listOf(
        "manufactureOfAgrochemicalPesticidesProducts",
        "landDegradationDesertificationSoilSealingExposure",
        "sustainableAgriculturePolicy",
        "sustainableOceansAndSeasPolicy",
        "threatenedSpeciesExposure",
        "biodiversityProtectionPolicy",
        "deforestationPolicy",
    )

    /**
     * Move the required fields from wastes to biodiversity
     */
    private fun migrateWasteToBiodiversity(dataset: JSONObject) {
        val wastes = dataset.getOrJavaNull("waste") as JSONObject? ?: return
        val biodiversity = dataset.getOrJavaNull("biodiversity") as JSONObject? ?: JSONObject()

        val keysToBeRemoved: MutableList<String> = mutableListOf()
        wastes.keys().forEach { subFieldKey ->
            if (subFieldKey in listOfWasteToBiodiversity) {
                val subField = wastes.getOrJavaNull(subFieldKey)
                biodiversity.put(subFieldKey, subField ?: JSONObject.NULL)
                keysToBeRemoved.add(subFieldKey)
            }
        }
        keysToBeRemoved.forEach { wastes.remove(it) }

        dataset.put("biodiversity", biodiversity)
        if (wastes.isEmpty) dataset.remove("waste")
    }

    private fun removeScopeOfEntities(dataset: JSONObject) {
        val general = dataset.getOrJavaNull("general") as JSONObject? ?: return
        val generalGeneral = general.getOrJavaNull("general") as JSONObject? ?: return
        val scopeOfEntities = generalGeneral.getOrJavaNull("scopeOfEntities")
        if (scopeOfEntities !== null) generalGeneral.remove("scopeOfEntities")
    }

    /**
     * Find the data points which are not extended and therefore do not have a mandatory
     * "quality" key.
     * If an object has "dataSource" or "value" keys without a "quality" key, they are considered BaseDataPoints
     * and are updated with a "quality" value
     */
    private fun checkRecursivelyForBaseDataPoint(
        dataset: JSONObject,
        objectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            var hasDataSourceOrValue = false
            var hasQuality = false
            obj.keys().forEach { key ->
                if (key == "dataSource") hasDataSourceOrValue = true
                if (key == "value") hasDataSourceOrValue = true
                if (key == "quality") hasQuality = true
            }
            if (hasDataSourceOrValue && !hasQuality) {
                obj.put("quality", "NA")
            }
            obj.keys().forEach {
                checkRecursivelyForBaseDataPoint(obj, it)
            }
        }
    }

    /**
     * Move some fields and make all datapoints extended
     */
    fun migrateSfdrData(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        val environmental = dataset.getOrJavaNull("environmental") as JSONObject?
        if (environmental !== null) migrateWasteToBiodiversity(environmental)

        removeScopeOfEntities(dataset)

        dataset.keys().forEach {
            checkRecursivelyForBaseDataPoint(dataset, it)
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
