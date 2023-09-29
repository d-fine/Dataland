package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Performs the migration of the refactored data point classes for the eu taxonomy non fiancials framework
 */
class V11_MigrateRefactoredDataPointClassesEuTaxoFinancials {
    /**
     * Performs the migration of the refactored data point classes for eu taxonomy financials data
     */
    fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "lksg",
            this::migrateRefactoredDataPointClasses,
        )
    }

    /*
    private val oldToNewFieldNamesForReports = mapOf(
        "reference" to "fileReference",
    ) */

    /**
     * Migrates the refactored Data Point Classes for the eu taxonomy non financials framework
     */
    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        // val migrationHelper = MigrationHelper()
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        // migrationHelper.migrateReferencedReports(dataObject, oldToNewFieldNamesForReports)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }
}
