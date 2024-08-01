package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing EU taxonomy non-financials datasets and migrates all
 * existing BaseDataPoints to ExtendedDataPoints.
 */
class V19__MigrateEutaxonomyNonFinancialsExtendedDatapoints : BaseJavaMigration() {
    private val relevantFields = listOf(
        "scopeOfEntities",
        "nfrdMandatory",
        "euTaxonomyActivityLevelReporting",
        "numberOfEmployees",
        "enablingShareInPercent",
        "transitionalShareInPercent",
        "relativeShareInPercent",
        "totalAmount",
        "substantialContributionToClimateChangeMitigationInPercent",
        "substantialContributionToClimateChangeAdaptationInPercent",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
        "substantialContributionToTransitionToACircularEconomyInPercent",
        "substantialContributionToPollutionPreventionAndControlInPercent",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
    )

    /**
     * Create a nested JSON object from a JSON object and a key.
     * @param json JSON object
     * @param key key corresponding to the JSON object
     */
    private fun createNestedJsonObject(json: JSONObject, key: String) {
        val nestedJson = JSONObject()
        nestedJson.put("value", json.getOrJavaNull(key))
        json.put(key, nestedJson)
    }

    /**
     * Find all relevant fields and make them extended data points.
     * @param dataset JSONObject
     * @param objectName key corresponding to the JSON object
     */
    private fun checkRecursivelyForBaseDataPoint(
        dataset: JSONObject,
        objectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            obj.keys().forEach {
                if (it in relevantFields) {
                    createNestedJsonObject(obj, it)
                }
                checkRecursivelyForBaseDataPoint(obj, it)
            }
        }
    }

    /**
     * Migrate a DataTableEntity so that certain BaseDataPoints are turned into ExtendedDataPoints.
     * @param dataTableEntity DataTableEntity
     */
    fun migrateEutaxonomyNonFinancialsData(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            checkRecursivelyForBaseDataPoint(dataset, it)
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-non-financials",
            migrate = this::migrateEutaxonomyNonFinancialsData,
        )
    }
}
