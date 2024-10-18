package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script changes the substring "Adaption" to "Adaptation" in all field names that are
 * "substantialContributionToClimateChangeAdaptionInPercent" in all eu-taxonomy-non-financial datasets
 */
@Suppress("ClassName")
class V14__MigrateSubstantialContributionToClimateChangeAdaptation : BaseJavaMigration() {
    private val mapOfOldToNewFieldNames =
        mapOf(
            "substantialContributionToClimateChangeAdaptionInPercent"
                to "substantialContributionToClimateChangeAdaptationInPercent",
        )

    private val pairOfOldToNewDnshFieldName = "dnshToClimateChangeAdaption" to "dnshToClimateChangeAdaptation"

    /**
     * Migrates substantialContributionToClimateChangeAdaption field to substantialContributionToClimateChangeAdaptation
     */
    @Suppress("NestedBlockDepth")
    fun migrateSubstantialContributionToClimateChangeAdaptation(dataTableEntity: DataTableEntity) {
        val euTaxoDataSet = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            mapOfOldToNewFieldNames.forEach {
                val euTaxoDataSubsetCashFlowType = euTaxoDataSet.getOrJavaNull(cashFlowType) ?: return@forEach
                euTaxoDataSubsetCashFlowType as JSONObject
                euTaxoDataSubsetCashFlowType.put(
                    it.value,
                    euTaxoDataSubsetCashFlowType.getOrJsonNull(it.key),
                )
                euTaxoDataSubsetCashFlowType.remove(it.key)
                if (euTaxoDataSubsetCashFlowType.getOrJavaNull("alignedActivities") != null) {
                    euTaxoDataSubsetCashFlowType.getJSONArray("alignedActivities").forEach { activity ->
                        activity as JSONObject
                        activity.put(it.value, activity[it.key])
                        activity.remove(it.key)
                        activity.put(
                            pairOfOldToNewDnshFieldName.second,
                            activity[pairOfOldToNewDnshFieldName.first],
                        )
                        activity.remove(pairOfOldToNewDnshFieldName.first)
                    }
                }
            }
        }
        dataTableEntity.companyAssociatedData.put("data", euTaxoDataSet.toString())
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context, "eutaxonomy-non-financials",
            this::migrateSubstantialContributionToClimateChangeAdaptation,
        )
    }
}
