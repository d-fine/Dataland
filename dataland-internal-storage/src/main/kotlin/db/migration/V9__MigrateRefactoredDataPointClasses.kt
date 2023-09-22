package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

class V9__MigrateRefactoredDataPointClasses : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials",
            this::migrateRefactoredDataPointClasses,
        )
    }

    private val oldToNewFieldNamesForReports = mapOf(
        "report" to "fileReference",
    )

    private val oldToNewFieldNamesForDocuments = oldToNewFieldNamesForReports

    private val oldToNewFieldNamesForAssurance = mapOf(
        "assurance" to "value",
    )

    /**
     * Migrate the data to new variable names in data point structures
     */
    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
        iterateThroughCashFlowCategories(dataObject)
        dataObject.getOrJavaNull("General")?.let {
            migrateReports(it as JSONObject)
            migrateAssurance(it)
        }
    }

    fun migrateReports(generalCategory: JSONObject) {
        val referencedReportsObject = generalCategory.getOrJsonNull("referencedReports")
        if (referencedReportsObject != JSONObject.NULL) {
            referencedReportsObject as JSONObject
            for (key in referencedReportsObject.keys()) {
                oldToNewFieldNamesForReports.forEach {
                    val oneReportObject = referencedReportsObject.getJSONObject(key)
                    oneReportObject.put(it.value, oneReportObject.get(it.key))
                    oneReportObject.put("reportName", key)
                }
            }
        }
    }

    fun iterateThroughCashFlowCategories(dataObject: JSONObject) {
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            val category = dataObject.getOrJavaNull(cashFlowType) ?: return@forEach
            migrateDataSource(category as JSONObject)
        }
    }

    fun migrateDataSource(grandParentObjectOfDataSource: JSONObject) {
        val parentOfDataSource = grandParentObjectOfDataSource.getOrJsonNull("totalAmount")
        parentOfDataSource as JSONObject
        val dataSourceObject = parentOfDataSource.getOrJsonNull("dataSource")
        dataSourceObject as JSONObject
        dataSourceObject.put("fileName", "name")
        oldToNewFieldNamesForDocuments.forEach {
            dataSourceObject.put(it.value, dataSourceObject.get(it.key))
        }
    }
    fun migrateAssurance(generalCategory: JSONObject) {
        val assuranceParentObject = generalCategory.getOrJsonNull("assurance")
        if (assuranceParentObject != JSONObject.NULL) {
            assuranceParentObject as JSONObject
            oldToNewFieldNamesForAssurance.forEach {
                assuranceParentObject.put(it.value, assuranceParentObject.get(it.key))
            }
            migrateDataSource(assuranceParentObject)
        }
    }
}
