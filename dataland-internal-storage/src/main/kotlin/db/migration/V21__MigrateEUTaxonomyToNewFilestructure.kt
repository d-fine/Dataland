package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates existing EUTaxonomyNonFinancial Files to the new
 * EUTaxonomyNonFinancials structure
 */

class V21__MigrateEUTaxonomyToNewFilestructure : BaseJavaMigration() {
    private val mapOfOldToNewFieldNames = mapOf(
        "substantialContributionToClimateChangeMitigationInPercent" to
            "substantialContributionToClimateChangeMitigationInPercentAligned",
        "substantialContributionToClimateChangeAdaptationInPercent" to
            "substantialContributionToClimateChangeAdaptationInPercentAligned",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" to
            "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentAligned",
        "substantialContributionToTransitionToACircularEconomyInPercent" to
            "substantialContributionToTransitionToACircularEconomyInPercentAligned",
        "substantialContributionToPollutionPreventionAndControlInPercent" to
            "substantialContributionToPollutionPreventionAndControlInPercentAligned",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" to
            "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentAligned",
    )

    private fun changeFields(revenueKeys: JSONObject) {
        mapOfOldToNewFieldNames.forEach {
            if (revenueKeys.has(it.key)) {
                revenueKeys.put(it.value, revenueKeys[it.key])
                revenueKeys.remove(it.key)
            }
        }
    }

    /**
     * Migrate all relevant keys under the array keys to the new naming convention
     */
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
