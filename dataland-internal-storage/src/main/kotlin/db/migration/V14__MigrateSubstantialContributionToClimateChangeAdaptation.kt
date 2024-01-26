package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.dataland.datalandbackend.openApiClient.model.Activity
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing SFDR datasets, more specifically it introduces an additional layer to
 * each sector in the high-impact climate sectors
 */
class V14__MigrateSubstantialContributionToClimateChangeAdaption : BaseJavaMigration() {
    private val mapOfOldToNewFieldNames = mapOf(
        "substantialContributionToClimateChangeAdaptionInPercent" to "substantialContributionToClimateChangeAdaptationInPercent",
    )
    private val mapOfOldToNewdDnshFieldName = mapOf(
        "dnshToClimateChangeAdaption" to "dnshToClimateChangeAdaptation",
    )

    /**
     * Migrates substantialContributionToClimateChangeAdaption field to substantialContributionToClimateChangeAdaptation
     */
    fun migrateSubstantialContributionToClimateChangeAdaption(dataTableEntity: DataTableEntity) {
        val companyAssociatedDatasetAsString = dataTableEntity.companyAssociatedData
        val euTaxoDataset = JSONObject(companyAssociatedDatasetAsString.getString("data"))
        val euTaxoDataset2 = euTaxoDataset.getOrJavaNull("data") as JSONObject

        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            mapOfOldToNewFieldNames.forEach {
                euTaxoDataset2.getJSONObject(cashFlowType).put(it.value, euTaxoDataset2.getJSONObject(cashFlowType).getInt(it.key))
                euTaxoDataset2.getJSONObject(cashFlowType).remove(it.key)
                euTaxoDataset2.getJSONObject(cashFlowType).getJSONArray("alignedActivities").forEach { actitivy ->
                    actitivy as JSONObject
                    actitivy.put(it.value, actitivy.get(it.key))
                    actitivy.remove(it.key)
                    actitivy.put(mapOfOldToNewdDnshFieldName.values.first(), actitivy.get(mapOfOldToNewdDnshFieldName.keys.first()))
                    actitivy.remove(mapOfOldToNewdDnshFieldName.keys.first())
                }
            }
        }
        euTaxoDataset.put("data",euTaxoDataset2)
        dataTableEntity.companyAssociatedData.put("data", euTaxoDataset.toString())
        println(dataTableEntity.companyAssociatedData)
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context, "eutaxonomy-non-financials",
            this::migrateSubstantialContributionToClimateChangeAdaption,
        )
    }
}
