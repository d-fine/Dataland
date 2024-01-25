package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
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


    /**
     * Migrates substantialContributionToClimateChangeAdaption field to substantialContributionToClimateChangeAdaptation
     */
    fun migrateSubstantialContributionToClimateChangeAdaption(dataTableEntity: DataTableEntity) {
        val companyAssociatedDatasetAsString = dataTableEntity.companyAssociatedData
        println("Here")
        println(companyAssociatedDatasetAsString)
        println("Here2")
        val euTaxoDataset = JSONObject(companyAssociatedDatasetAsString.getString("data"))
        println(euTaxoDataset)
        println("Here3")
        val euTaxoDataset2 = euTaxoDataset.getOrJavaNull("data") as JSONObject
        println(euTaxoDataset2)


        //TODO go into revenue, capex, opex
        listOf("revenue").forEach { cashFlowType -> //, "capex", "opex"
            mapOfOldToNewFieldNames.forEach {
//                euTaxoDataset.put(it.value, euTaxoDataset.get(it.key))
                val euTaxoDataSetRename = euTaxoDataset2.getJSONObject(cashFlowType)
                euTaxoDataset2.getJSONObject(cashFlowType).put(it.value, euTaxoDataset2.getJSONObject(cashFlowType).getInt(it.key))
                euTaxoDataset2.getJSONObject(cashFlowType).remove(it.key)
            }
        }
        println("Here5")
        println(euTaxoDataset2)
        dataTableEntity.companyAssociatedData.put("data", euTaxoDataset2.toString())

    }
    /**
     * This function iterates through all cash flow categories in order to migrate the "DataSource" Object
     */


    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context, "eutaxonomy-non-financials",
            this::migrateSubstantialContributionToClimateChangeAdaption,
        )
    }
}
