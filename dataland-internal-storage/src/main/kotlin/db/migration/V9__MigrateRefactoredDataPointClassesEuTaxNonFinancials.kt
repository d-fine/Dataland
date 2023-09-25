package db.migration

import db.migration.utils.DataTableEntity
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
     * Migrate the data to new variable names in data point structures
     */

    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        val generalCategoryObject = dataObject.getOrJavaNull("general")
        generalCategoryObject as JSONObject
        migrateReports(generalCategoryObject)
        migrateAssurance(dataObject)
        iterateThroughCashFlowCategories(dataObject)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    private fun migrateReports(generalCategory: JSONObject) {
        val referencedReportsObject = generalCategory.getOrJsonNull("referencedReports")
        if (referencedReportsObject != JSONObject.NULL) {
            referencedReportsObject as JSONObject
            for (key in referencedReportsObject.keys()) {
                oldToNewFieldNamesForReports.forEach {
                    val oneReportObject = referencedReportsObject.getJSONObject(key)
                    oneReportObject.put(it.value, oneReportObject.get(it.key))
                    oneReportObject.put("fileName", key)
                    oneReportObject.remove(it.key)
                }
            }
        }
    }

    private fun iterateThroughCashFlowCategories(dataObject: JSONObject) {
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            val categoryObject = dataObject.getOrJavaNull(cashFlowType) ?: return@forEach
            categoryObject as JSONObject
            val parentObjectOfDataSource = categoryObject.getOrJsonNull("totalAmount")
            migrateDataSource(parentObjectOfDataSource as JSONObject, dataObject)
        }
    }

    private fun migrateDataSource(parentObjectOfDataSource: JSONObject, dataObject: JSONObject) {
        val dataSourceObject = parentObjectOfDataSource.getOrJsonNull("dataSource")
        dataSourceObject as JSONObject
        val fileNameToSearchInReferencedReports: String = dataSourceObject.get("report") as String
        dataSourceObject.put(
            "fileReference",
            getFileReferenceFromReferencedReports(
                fileNameToSearchInReferencedReports, dataObject,
            ),
        )
        oldToNewFieldNamesForDocuments.forEach {
            dataSourceObject.put(it.value, dataSourceObject.get(it.key))
            dataSourceObject.remove(it.key)
        }
    }
    private fun migrateAssurance(dataObject: JSONObject) {
        val generalCategoryObject = dataObject.getOrJavaNull("general")
        generalCategoryObject as JSONObject
        val assuranceParentObject = generalCategoryObject.getOrJsonNull("assurance")
        if (assuranceParentObject != JSONObject.NULL) {
            assuranceParentObject as JSONObject
            oldToNewFieldNamesForAssurance.forEach {
                assuranceParentObject.put(it.value, assuranceParentObject.get(it.key))
                assuranceParentObject.remove(it.key)
            }
            migrateDataSource(assuranceParentObject, dataObject)
        }
    }

    private fun getFileReferenceFromReferencedReports(fileName: String, dataObject: JSONObject): String {
        if (fileName != "") {
            val generalCategoryObject = dataObject.getOrJavaNull("general")
            generalCategoryObject as JSONObject
            val referencedReportsObject = generalCategoryObject.getOrJsonNull("referencedReports")
            referencedReportsObject as JSONObject
            val reportObject = referencedReportsObject.getOrJsonNull(fileName)
            reportObject as JSONObject
            return reportObject.getOrJsonNull("fileReference") as String
        }
        return ""
    }
}
