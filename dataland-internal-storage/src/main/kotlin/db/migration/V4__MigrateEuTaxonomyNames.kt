package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing eutaxonomy datasets to be in line again with the dataDictionary after
 * two fields have been renamed
 */
@Suppress("ClassName")
class V4__MigrateEuTaxonomyNames : BaseJavaMigration() {
    private val mapOfOldToNewFieldNames =
        mapOf(
            "reportingObligation" to "nfrdMandatory",
            "activityLevelReporting" to "euTaxonomyActivityLevelReporting",
        )

    private val dataTypesToMigrate =
        listOf(
            "eutaxonomy-non-financials",
            "eutaxonomy-financials",
        )

    /**
     * Migrates an old eu taxonomy  dataset to the new name
     */
    fun migrateEuTaxonomyNames(dataTableEntity: DataTableEntity) {
        val companyAssociatedDatasetAsString = dataTableEntity.companyAssociatedData
        val euTaxoDataset = JSONObject(companyAssociatedDatasetAsString.getString("data"))
        mapOfOldToNewFieldNames.forEach {
            euTaxoDataset.put(it.value, euTaxoDataset.get(it.key))
            euTaxoDataset.remove(it.key)
        }
        dataTableEntity.companyAssociatedData.put("data", euTaxoDataset.toString())
    }

    override fun migrate(context: Context?) {
        dataTypesToMigrate.forEach { dataType: String ->
            migrateCompanyAssociatedDataOfDatatype(context, dataType, this::migrateEuTaxonomyNames)
        }
    }
}
