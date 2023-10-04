package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.MigrationHelper
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script migrates the eu taxonomy non financials data to the various new class structures for
 * data points
 */
class V9__MigrateRefactoredDataPointClassesEuTaxNonFinancials : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials",
            this::migrateRefactoredDataPointClasses,
        )
    }

    private val oldToNewFieldNamesForReports = mapOf(
        "reference" to "fileReference",
    )

    private val oldToNewFieldNamesForDocuments = mapOf(
        "report" to "fileName",
    )

    private val oldToNewFieldNamesForAssurance = mapOf(
        "assurance" to "value",
    )

    /**
     * Migrates the data to new variable names in data point structures
     */
    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        val migrationHelper = MigrationHelper()
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        val generalCategoryObject = dataObject.getOrJavaNull("general")
        generalCategoryObject as JSONObject
        migrationHelper.migrateReferencedReports(generalCategoryObject, oldToNewFieldNamesForReports)
        migrateAssurance(dataObject, oldToNewFieldNamesForDocuments, migrationHelper)
        iterateThroughCashFlowCategories(dataObject, oldToNewFieldNamesForDocuments, migrationHelper)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    /**
     * This function iterates through all cash flow categories in order to migrate the "DataSource" Object
     */
    private fun iterateThroughCashFlowCategories(
        dataObject: JSONObject,
        migrationFieldNames: Map<String, String>,
        migrationHelper: MigrationHelper,
    ) {
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            val categoryObject = dataObject.getOrJavaNull(cashFlowType) ?: return@forEach
            categoryObject as JSONObject
            val parentObjectOfDataSource = categoryObject.getOrJavaNull("totalAmount") ?: return
            migrationHelper.migrateOneSingleObjectOfDataSource(
                parentObjectOfDataSource as JSONObject, dataObject,
                migrationFieldNames,
            )
        }
    }

    /**
     * This function migrates the "Assurance" Object including a "DataSource" Object
     */
    private fun migrateAssurance(
        dataObject: JSONObject,
        migrationFieldNames: Map<String, String>,
        migrationHelper: MigrationHelper,
    ) {
        val generalCategoryObject = dataObject.getOrJavaNull("general") ?: return
        generalCategoryObject as JSONObject
        val assuranceParentObject = generalCategoryObject.getOrJavaNull("assurance") ?: return
        assuranceParentObject as JSONObject
        oldToNewFieldNamesForAssurance.forEach {
            if (assuranceParentObject.has(it.key)) {
                assuranceParentObject.put(it.value, assuranceParentObject.get(it.key))
                assuranceParentObject.remove(it.key)
            }
        }
        migrationHelper.migrateOneSingleObjectOfDataSource(assuranceParentObject, dataObject, migrationFieldNames)
    }
}
