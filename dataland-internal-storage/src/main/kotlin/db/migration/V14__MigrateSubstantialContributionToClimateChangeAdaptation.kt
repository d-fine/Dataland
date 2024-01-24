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
     * Migrates substantialContributionToClimateChangeAdapation field to substantialContributionToClimateChangeAdaptation
     */
    fun migrateSubstantialContributionToClimateChangeAdaption(dataTableEntity: DataTableEntity) {
        val companyAssociatedDatasetAsString = dataTableEntity.companyAssociatedData
        val euTaxoDataset = JSONObject(companyAssociatedDatasetAsString.getString("data"))
        mapOfOldToNewFieldNames.forEach {
            euTaxoDataset.put(it.value, euTaxoDataset.get(it.key))
            euTaxoDataset.remove(it.key)
        }
        dataTableEntity.companyAssociatedData.put("data", euTaxoDataset.toString())
    }
    override fun migrate(context: Context?) {
            migrateCompanyAssociatedDataOfDatatype(context, "eutaxonomy-non-financials", this::migrateSubstantialContributionToClimateChangeAdaption)
    }
}
