package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Performs the migration of the refactored data point classes for the lksg framework
 */
@Suppress("ClassName")
class V10__MigrateRefactoredDataPointClassesLksg : BaseJavaMigration() {
    /**
     * Performs the migration of the refactored data point classes for lksg data
     */
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "lksg",
            this::migrateRefactoredDataPointClasses,
        )
    }

    private val oldToNewFieldNamesForDataSource =
        mapOf(
            "name" to "fileName",
            "reference" to "fileReference",
        )

    /**
     * Migrates the refactored Data Point Classes for the Lksg framework
     */
    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        if (dataObject != JSONObject.NULL) {
            recurseThroughDataToDataSource(dataObject, "dataSource")
        }
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    private fun recurseThroughDataToDataSource(
        dataObject: JSONObject,
        targetKey: String,
    ) {
        for (key in dataObject.keys()) {
            if (dataObject.getOrJavaNull(key) !is JSONObject) {
                continue
            }
            if (key == targetKey) {
                migrateSingleDataSourceObject(dataObject.getJSONObject(targetKey))
            } else {
                recurseThroughDataToDataSource(dataObject.getJSONObject(key), targetKey)
            }
        }
    }

    /**
     * Migrates one single dataSource Object to the new data point class structure
     */
    private fun migrateSingleDataSourceObject(dataSourceObject: JSONObject) {
        oldToNewFieldNamesForDataSource.forEach {
            dataSourceObject.put(it.value, dataSourceObject[it.key])
            dataSourceObject.remove(it.key)
        }
    }
}
