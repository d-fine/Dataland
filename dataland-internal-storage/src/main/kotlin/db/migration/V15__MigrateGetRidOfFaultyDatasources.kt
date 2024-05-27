package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script removes all file references that are not hashes and all
 * file references that are hashes but do not exist.
 */
class V15__MigrateGetRidOfFaultyDatasources : BaseJavaMigration() {

    private val regex = "^[a-fA-F0-9]{64}$".toRegex()

    private val dataTypesToMigrate = listOf(
        "eutaxonomy-non-financials",
        "eutaxonomy-financials",
        "sfdr",
        "lksg",
        "esg-questionnaire",
        "heimathafen",
        "sme",

        )

    // todo the actual file references needs to be fed to the list. now there are only dummies.
    private val fileReferencesThatAreHashesButDoNotExist = listOf(
        "3271890987321798",
        "189237128391823",
        "0931209801392093",
    )

    override fun migrate(context: Context?) {
        dataTypesToMigrate.forEach {
            migrateCompanyAssociatedDataOfDatatype(
                context,
                it,
                this::migrateFaultyFileReferences,
            )
        }
    }

    private fun checkForFaultyFileReferenceAndIterateFurther(
        dataset: JSONObject,
        objectName: String,
        targetObjectName: String,
    ) {
        val obj = dataset.getOrJavaNull(objectName)
        if (obj !== null && obj is JSONObject) {
            val dataSource = obj.getOrJavaNull(targetObjectName) as JSONObject?
            if (dataSource !== null) {
                val fileReference = dataSource.get("fileReference") as String
                if (!isSha256(fileReference) || fileReference in fileReferencesThatAreHashesButDoNotExist) {
                    obj.put(targetObjectName, null as Any?)
                }
            } else {
                obj.keys().forEach { checkForFaultyFileReferenceAndIterateFurther(obj, it, targetObjectName) }
            }
        }
    }

    private fun isSha256(fileReference: String): Boolean {
        return fileReference.matches(regex)
    }

    /**
     * Migrate the data points with blank file references to containing null-valued a dataSource instead
     */
    fun migrateFaultyFileReferences(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        dataset.keys().forEach {
            checkForFaultyFileReferenceAndIterateFurther(
                dataset, it, "dataSource"
            )
        }
        dataset.keys().forEach {
            checkForFaultyFileReferenceAndIterateFurther(
                dataset, it, "companyReport"
            )
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

}
