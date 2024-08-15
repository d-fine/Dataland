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
class V20__MigrateEutaxonomyNonFinancialsExtendedDatapoints : BaseJavaMigration() {

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
        "alignedActivities",
        "nonAlignedActivities",
    )

    /**
     * Move key: object to key: { "value": object }
     * @param jsonObject JSON object
     * @param key key corresponding to the JSON object
     */
    private fun updateObjectBehindKeyInJsonObject(jsonObject: JSONObject, key: String) {
        val newValue = JSONObject()
        val oldValue = jsonObject[key]
        if (oldValue != JSONObject.NULL) {
            newValue.put("value", oldValue)
            jsonObject.put(key, newValue)
        }
    }

    /**
     * Check if the keys of a JSON object are relevant fields and, if so, update the object behind these keys.
     * @param jsonObject JSON object
     */
    private fun checkForRelevantFieldsInJsonObjectKeys(jsonObject: JSONObject) {
        jsonObject.keys().forEach {
            if (it == "absoluteShare" && jsonObject[it] != JSONObject.NULL) {
                val absoluteShare = jsonObject[it] as JSONObject
                val amount = absoluteShare["amount"]
                absoluteShare.remove("amount")
                absoluteShare.put("value", amount)
            } else if (it != "absoluteShare" && it in relevantFields) {
                updateObjectBehindKeyInJsonObject(jsonObject, it)
            } else {
                // Do nothing as no more migration is required
            }
            checkRecursivelyForRelevantFieldKeysInJsonObject(jsonObject, it)
        }
    }

    /**
     * Check recursively for relevant field keys in a JSON array.
     * @param jsonArray JSON array
     */
    private fun checkRecursivelyForRelevantFieldKeysInJsonArray(jsonArray: JSONArray) {
        jsonArray.forEach {
            if (it != null && it is JSONObject) {
                checkForRelevantFieldsInJsonObjectKeys(it)
            }
        }
    }

    /**
     * Check recursively for relevant field keys in a JSON object.
     * @param jsonObject JSON object
     * @param key key corresponding to the JSON object
     */
    private fun checkRecursivelyForRelevantFieldKeysInJsonObject(jsonObject: JSONObject, key: String) {
        val obj = jsonObject.getOrJavaNull(key)
        if (obj !== null && obj is JSONObject) {
            checkForRelevantFieldsInJsonObjectKeys(obj)
        } else if (obj != null && obj is JSONArray) {
            checkRecursivelyForRelevantFieldKeysInJsonArray(obj)
        } else {
            // Do nothing as no more migration is required
        }
    }

    /**
     * Migrate a DataTableEntity so that the relevant fields are turned into ExtendedDataPoints.
     * @param dataTableEntity DataTableEntity
     */
    fun migrateEutaxonomyNonFinancialsData(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        checkForRelevantFieldsInJsonObjectKeys(jsonObject)
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
