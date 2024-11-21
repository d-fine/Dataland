package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing SFDR datasets, more specifically it sets the full dataSource objects to
 * null whenever the corresponding fileReferences are given by an empty string
 */
@Suppress("ClassName")
class V13__MigrateBlankFileReferencesInSfdr : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateBlankFileReferences,
        )
    }

    private fun checkForBlankFileReferenceAndIterateFurther(
        dataset: JSONObject,
        objectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            val dataSource = obj.getOrJavaNull("dataSource") as JSONObject?
            if (dataSource !== null) {
                if (dataSource.get("fileReference") == "") {
                    obj.put("dataSource", null as Any?)
                }
            } else {
                obj.keys().forEach { checkForBlankFileReferenceAndIterateFurther(obj, it) }
            }
        }
    }

    /**
     * Migrate the data points with blank file references to containing null-valued a dataSource instead
     */
    fun migrateBlankFileReferences(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach { checkForBlankFileReferenceAndIterateFurther(dataset, it) }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
