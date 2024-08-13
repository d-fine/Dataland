package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * This migration script updates existing EUTaxonomyNonFinancial Files to the new
 * EUTaxonomyNonFinancials structure
 */


class V20__MigrateEUTaxonomyToNewFilestructure : BaseJavaMigration() {

    private val mapOfOldToNewFieldNames = mapOf(
        "substantialContributionToClimateChangeMitigationInPercent" to "substantialContributionToClimateChangeMitigationInPercentAligned",
        "substantialContributionToClimateChangeAdaptationInPercent" to "substantialContributionToClimateChangeAdaptationInPercentAligned",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" to "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentAligned",
        "substantialContributionToTransitionToACircularEconomyInPercent" to "substantialContributionToTransitionToACircularEconomyInPercentAligned",
        "substantialContributionToPollutionPreventionAndControlInPercent" to "substantialContributionToPollutionPreventionAndControlInPercentAligned",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" to "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentAligned",
        //TODO add third green block
    )

    private fun changeFields(revenueKeys: JSONObject) {
        mapOfOldToNewFieldNames.forEach {
            revenueKeys.put(it.value, revenueKeys.get(it.key))
            revenueKeys.remove(it.key)
        }
    }


    fun migrateEutaxonomyNonFinancialsData(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        arrayOf("opex", "capex", "revenue").forEach { //
            val revenueKeys = jsonObject.getOrJavaNull(it)
            if (revenueKeys != null && revenueKeys is JSONObject) {
                changeFields(revenueKeys)
            }
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