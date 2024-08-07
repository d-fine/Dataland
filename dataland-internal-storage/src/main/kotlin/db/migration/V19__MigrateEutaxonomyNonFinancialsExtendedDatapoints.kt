package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
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
        "substantialContributionToClimateChangeMitigationInPercent",
        "substantialContributionToClimateChangeAdaptationInPercent",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
        "substantialContributionToTransitionToACircularEconomyInPercent",
        "substantialContributionToPollutionPreventionAndControlInPercent",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
        "absoluteShare",
        "fiscalYearEnd",
        "fiscalYearDeviation",
    )

    /**
     * Create a nested JSON object from a JSON object and a key.
     * @param jsonObject JSON object
     * @param key key corresponding to the JSON object
     */
    private fun createNestedJsonObject(jsonObject: JSONObject, key: String) {
        val nestedJson = JSONObject()
        nestedJson.put("value", jsonObject.getOrJavaNull(key))
        jsonObject.put(key, nestedJson)
    }

    /**
     * Check if the keys of a JSON object are relevant fields and, if so, created a nested JSON object.
     * @param jsonObject JSON object
     */
    private fun checkForRelevantFieldsInJsonObjectKeys(jsonObject: JSONObject) {
        jsonObject.keys().forEach {
            if (it == "absoluteShare") {
                val absoluteShare = jsonObject[it] as JSONObject
                val amount = absoluteShare["amount"]
                absoluteShare.remove("amount")
                absoluteShare.put("value", amount)
            } else if (it in relevantFields) {
                createNestedJsonObject(jsonObject, it)
            } else {
                // Do nothing
            }
            checkRecursivelyForBaseDataPointsInJsonObject(jsonObject, it)
        }
    }

    /**
     * Check recursively for BaseDataPoints in a JSON array.
     * @param jsonArray JSON array
     */
    private fun checkRecursivelyForBaseDataPointsInJsonArray(jsonArray: JSONArray) {
        for (i in 0 until jsonArray.length()) {
            val element = jsonArray[i]
            if (element != null && element is JSONObject) {
                checkForRelevantFieldsInJsonObjectKeys(element)
            }
        }
    }

    /**
     * Check recursively for BaseDataPoints in a JSON object.
     * @param jsonObject JSON object
     * @param key key corresponding to the JSON object
     */
    private fun checkRecursivelyForBaseDataPointsInJsonObject(jsonObject: JSONObject, key: String) {
        val obj = jsonObject.getOrJavaNull(key)
        if (obj !== null && obj is JSONObject) {
            checkForRelevantFieldsInJsonObjectKeys(obj)
        } else if (obj != null && obj is JSONArray) {
            checkRecursivelyForBaseDataPointsInJsonArray(obj)
        } else {
            // Do nothing
        }
    }

    /**
     * Migrate a DataTableEntity so that certain BaseDataPoints are turned into ExtendedDataPoints.
     * @param dataTableEntity DataTableEntity
     */
    fun migrateEutaxonomyNonFinancialsData(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        jsonObject.keys().forEach {
            checkRecursivelyForBaseDataPointsInJsonObject(jsonObject, it)
        }
        dataTableEntity.companyAssociatedData.put("data", jsonObject.toString())
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-non-financials",
            migrate = this::migrateEutaxonomyNonFinancialsData,
        )
    }
}
