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
@Suppress("ClassName")
class V16__MigrateSfdrMinorChangesOnlyExtendedDatapoints : BaseJavaMigration() {
    private val frameworksToMigrateDataPointsNoSfdr =
        listOf(
            "eutaxonomy-non-financials",
            "eutaxonomy-financials",
            "sme",
        )

    private val listOfWasteToBiodiversity =
        listOf(
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
        if (obj !== null && obj is JSONObject) {
            var quality = null as String?
            if (obj.has("quality")) {
                quality = obj.getOrJavaNull("quality").toString()
            }
            if (quality == null || quality == "NA") {
                obj.remove("quality")
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
            "sfdr",
            this::migrateSfdrData,
        )
        frameworksToMigrateDataPointsNoSfdr.forEach {
            migrateCompanyAssociatedDataOfDatatype(
                context,
                it,
                this::migrateDataPoints,
            )
        }
    }
}
