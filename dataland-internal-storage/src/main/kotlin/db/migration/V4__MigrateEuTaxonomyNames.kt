package db.migration

import db.migration.utils.getCompanyAssociatedDatasetsForDataType
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing eutaxonomy datasets to be in line again with the dataDictionary after
 * two fields have been renamed
 */
class V4__MigrateEuTaxonomyNames : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val mapOfOldToNewFieldNames = mapOf(
            "reportingObligation" to "nfrdMandatory",
            "activityLevelReporting" to "euTaxonomyActivityLevelReporting",
        )

        val dataTypesToMigrate = listOf(
            "eutaxonomy-non-financials",
            "eutaxonomy-financials",
        )
        dataTypesToMigrate.forEach { dataType: String ->
            val companyAssociatedDatasets = getCompanyAssociatedDatasetsForDataType(context, dataType)
            companyAssociatedDatasets.forEach {
                val companyAssociatedDatasetAsString = it.companyAssociatedData.toString()
                val companyAssociatedDatasetWithEscapedSingleQuotes =
                    JSONObject(companyAssociatedDatasetAsString.replace("'", "''"))
                var euTaxoDataset = JSONObject(companyAssociatedDatasetWithEscapedSingleQuotes.getString("data"))
                mapOfOldToNewFieldNames.forEach {
                    euTaxoDataset.put(it.value, euTaxoDataset.get(it.key))
                    euTaxoDataset.remove(it.key)
                }
                it.companyAssociatedData.put("data", euTaxoDataset.toString())
                context!!.connection.createStatement().execute(it.getWriteQuery())
            }
        }
    }
}
