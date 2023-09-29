package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.MigrationHelper
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
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
        migrateAssurance(dataObject)
        iterateThroughCashFlowCategories(dataObject)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    /**
     * This function iterates through all cash flow categories in order to migrate the "DataSource" Object
     */
    private fun iterateThroughCashFlowCategories(dataObject: JSONObject) {
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            val categoryObject = dataObject.getOrJavaNull(cashFlowType) ?: return@forEach
            categoryObject as JSONObject
            val parentObjectOfDataSource = categoryObject.getOrJavaNull("totalAmount") ?: return
            migrateDataSource(parentObjectOfDataSource as JSONObject, dataObject)
        }
    }

    /**
     * This function migrates the "DataSource" Object by amending variable names
     */
    private fun migrateDataSource(parentObjectOfDataSource: JSONObject, dataObject: JSONObject) {
        val dataSourceObject = parentObjectOfDataSource.getOrJavaNull("dataSource") ?: return
        dataSourceObject as JSONObject
        if (dataSourceObject.has("report")) {
            val fileNameToSearchInReferencedReports: String = dataSourceObject.get("report") as String
            dataSourceObject.put(
                "fileReference",
                getFileReferenceFromReferencedReports(
                    fileNameToSearchInReferencedReports, dataObject,
                ),
            )
        }
        oldToNewFieldNamesForDocuments.forEach {
            if (dataSourceObject.has(it.key)) {
                dataSourceObject.put(it.value, dataSourceObject.get(it.key))
                dataSourceObject.remove(it.key)
            }
        }
    }

    /**
     * This function migrates the "Assurance" Object including a "DataSource" Object
     */
    private fun migrateAssurance(dataObject: JSONObject) {
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
        migrateDataSource(assuranceParentObject, dataObject)
    }

    /**
     * This function reads the fileReference hash from referenced reports in order to store it
     * in the "DataSource" Object.
     */
    private fun getFileReferenceFromReferencedReports(fileName: String, dataObject: JSONObject): String {
        if (fileName != "") {
            val generalCategoryObject = dataObject.getOrJsonNull("general")
            generalCategoryObject as JSONObject
            val referencedReportsObject = generalCategoryObject.getOrJsonNull("referencedReports")
            referencedReportsObject as JSONObject
            val reportObject = referencedReportsObject.getOrJsonNull(fileName)
            if (reportObject != JSONObject.NULL) {
                reportObject as JSONObject
                return reportObject.getOrJsonNull("fileReference") as String
            }
        }
        return ""
    }
}
