package db.migration

import db.migration.utils.getCompanyAssociatedDatasetsForDataType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
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
            DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
            DataTypeEnum.eutaxonomyMinusFinancials,
        )
        dataTypesToMigrate.forEach { dataType: DataTypeEnum ->
            val companyAssociatedDatasets = getCompanyAssociatedDatasetsForDataType(context, dataType)
            companyAssociatedDatasets.forEach {
                var dataset = JSONObject(it.companyAssociatedData.getString("data"))
                mapOfOldToNewFieldNames.forEach {
                    dataset.put(it.value, dataset.get(it.key))
                    dataset.remove(it.key)
                }
                it.companyAssociatedData.put("data", dataset.toString())
                context!!.connection.createStatement().execute(it.getWriteQuery())
            }
        }
    }
}
