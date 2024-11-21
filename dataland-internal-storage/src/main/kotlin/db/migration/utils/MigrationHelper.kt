package db.migration.utils

import org.json.JSONObject

/**
 * This class provides functionality to help with common issues that might occur while writing a migration script.
 * It provides methods to transform data and rename fields.
 * It provides a buffer for fields that should be removed at a later point which is not possible immediately
 * when iterating over an objects keys
 */
class MigrationHelper {
    private data class FieldToRemove(
        val fieldHolder: JSONObject,
        val fieldName: String,
    )

    private val fieldsToRemove = mutableListOf<FieldToRemove>()

    /**
     * Queues a field of an object for later removal
     * @param fieldHolder the object that holds the field to remove
     * @param fieldName the name of the field to remove
     */
    fun queueFieldForRemoval(
        fieldHolder: JSONObject,
        fieldName: String,
    ) {
        fieldsToRemove.add(FieldToRemove(fieldHolder, fieldName))
    }

    /**
     * Removes all queued fields
     */
    fun removeQueuedFields() {
        fieldsToRemove.forEach {
            it.fieldHolder.remove(it.fieldName)
        }
        fieldsToRemove.clear()
    }

    /**
     * Migrates the transformed value of a field to a new field in the same object
     * @param fieldHolder the object that contains both fields
     * @param newFieldName the name of the field to migrate
     * @param oldFieldName the name of the new field
     * @param transformation the transformation to be applied to the original data before migration
     */
    inline fun <O, reified N> migrateValueFromTo(
        fieldHolder: JSONObject,
        oldFieldName: String,
        newFieldName: String,
        transformation: (O) -> N = {
            require(it is N)
            it
        },
    ) {
        fieldHolder.put(newFieldName, transformation((fieldHolder.getOrJavaNull(oldFieldName) ?: return) as O))
    }

    /**
     * Transforms the value of a field
     * @param fieldHolder the object that contains both fields
     * @param fieldName the name of the field whose data to transform
     * @param transformation the transformation to be applied to the original data before migration
     */
    inline fun <reified T> migrateValue(
        fieldHolder: JSONObject,
        fieldName: String,
        transformation: (T) -> T = { it },
    ) {
        migrateValueFromTo(fieldHolder, fieldName, fieldName, transformation)
    }

    /**
     * Migrates the transformed value of a field to a new field in the same object and queues the old field for removal
     * @param fieldHolder the object that contains both fields
     * @param newFieldName the name of the field to migrate
     * @param oldFieldName the name of the new field
     * @param transformation the transformation to be applied to the original data before migration
     */
    inline fun <O, reified N> migrateValueFromToAndQueueForRemoval(
        fieldHolder: JSONObject,
        oldFieldName: String,
        newFieldName: String,
        transformation: (O) -> N = {
            require(it is N)
            it
        },
    ) {
        migrateValueFromTo(fieldHolder, oldFieldName, newFieldName, transformation)
        queueFieldForRemoval(fieldHolder, oldFieldName)
    }

    /**
     * Migrates the transformed value of a data point to a new data point in the same object
     * and queues the old field for removal
     * @param dataPointHolder the object that contains both data points
     * @param oldDataPointName the name of the data point to migrate
     * @param newDataPointName the name of the new data point
     * @param transformation the transformation to be applied to the original data before migration
     */
    inline fun <O, reified N> migrateDataPointValueFromToAndQueueForRemoval(
        dataPointHolder: JSONObject,
        oldDataPointName: String,
        newDataPointName: String,
        transformation: (O) -> N = {
            require(it is N)
            it
        },
    ) {
        migrateDataPointValueFromTo(dataPointHolder, oldDataPointName, newDataPointName, transformation)
        queueFieldForRemoval(dataPointHolder, oldDataPointName)
    }

    /**
     * Migrates the transformed value of a data point to a new data point in the same object
     * @param dataPointHolder the object that contains both data points
     * @param oldDataPointName the name of the data point to migrate
     * @param newDataPointName the name of the new data point
     * @param transformation the transformation to be applied to the original data before migration
     */
    inline fun <O, reified N> migrateDataPointValueFromTo(
        dataPointHolder: JSONObject,
        oldDataPointName: String,
        newDataPointName: String,
        transformation: (O) -> N = {
            require(it is N)
            it
        },
    ) {
        val oldDataPointObject = (dataPointHolder.getOrJavaNull(oldDataPointName) ?: return) as JSONObject
        val newDataPointObject = JSONObject(oldDataPointObject.toString())
        migrateValueFromTo(newDataPointObject, "value", "value", transformation)
        dataPointHolder.put(newDataPointName, newDataPointObject)
    }

    /**
     * This function migrates the referenced reports by amending variable names
     */
    fun migrateReferencedReports(
        parentCategoryOfReferencedReports: JSONObject,
        fieldNamesToMigrate: Map<String, String>,
    ) {
        val referencedReportsObject =
            parentCategoryOfReferencedReports
                .getOrJavaNull("referencedReports") ?: return
        referencedReportsObject as JSONObject
        iterateThroughReferencedReports(referencedReportsObject, fieldNamesToMigrate)
    }

    /**
     * This function migrates the "Assurance" Object including a "DataSource" Object
     */
    fun migrateAssurance(
        dataObject: JSONObject,
        migrationFieldNamesForAssurance: Map<String, String>,
        migrationFieldNamesForReports: Map<String, String>,
        migrationHelper: MigrationHelper,
        framework: String,
    ) {
        var parentObject = dataObject
        if (framework == "euTaxonomyNonFinancials") {
            val generalCategoryObject = dataObject.getOrJavaNull("general") ?: return
            generalCategoryObject as JSONObject
            parentObject = generalCategoryObject
        } else {
            check(framework == "euTaxonomyFinancials") {
                "Migration of assurance may not be implemented for " +
                    "this framework"
            }
        }
        val assuranceParentObject = parentObject.getOrJavaNull("assurance") ?: return
        assuranceParentObject as JSONObject
        migrationFieldNamesForAssurance.forEach {
            if (assuranceParentObject.has(it.key)) {
                assuranceParentObject.put(it.value, assuranceParentObject[it.key])
                assuranceParentObject.remove(it.key)
            }
        }
        migrationHelper.migrateOneSingleObjectOfDataSource(
            assuranceParentObject, dataObject,
            migrationFieldNamesForReports, framework,
        )
    }

    /**
     * This function migrates the "DataSource" Object by amending variable names
     */
    fun migrateOneSingleObjectOfDataSource(
        parentObjectOfDataSource: JSONObject,
        dataObject: JSONObject,
        migrationFieldNames: Map<String, String>,
        framework: String,
    ) {
        val dataSourceObject = parentObjectOfDataSource.getOrJavaNull("dataSource") ?: return
        dataSourceObject as JSONObject
        if (dataSourceObject.has("report")) {
            val fileNameToSearchInReferencedReports: String = dataSourceObject["report"] as String
            dataSourceObject.put(
                "fileReference",
                getFileReferenceFromReferencedReports(
                    fileNameToSearchInReferencedReports, dataObject, framework,
                ),
            )
        }
        migrationFieldNames.forEach {
            if (dataSourceObject.has(it.key)) {
                dataSourceObject.put(it.value, dataSourceObject[it.key])
                dataSourceObject.remove(it.key)
            }
        }
    }

    /**
     * This function reads the fileReference hash from referenced reports in order to store it
     * in the "DataSource" Object.
     */
    private fun getFileReferenceFromReferencedReports(
        fileName: String,
        dataObject: JSONObject,
        framework: String,
    ): String {
        if (fileName != "") {
            var parentObject = dataObject
            if (framework == "euTaxonomyNonFinancials") {
                val generalCategoryObject = dataObject.getOrJsonNull("general")
                generalCategoryObject as JSONObject
                parentObject = generalCategoryObject
            } else {
                check(framework == "euTaxonomyFinancials") {
                    "Retrieval of reference from reports may not be implemented" +
                        " for this framework"
                }
            }
            val referencedReportsObject = parentObject.getOrJsonNull("referencedReports")
            referencedReportsObject as JSONObject
            val reportObject = referencedReportsObject.getOrJsonNull(fileName)
            if (reportObject != JSONObject.NULL) {
                reportObject as JSONObject
                return reportObject.getOrJsonNull("fileReference") as String
            }
        }
        return ""
    }

    /**
     * Iterates through all referenced reports to migrate reports
     */
    private fun iterateThroughReferencedReports(
        referencedReportsObject: JSONObject,
        fieldNamesToMigrate: Map<
            String,
            String,
        >,
    ) {
        for (key in referencedReportsObject.keys()) {
            fieldNamesToMigrate.forEach {
                val oneReportObject = referencedReportsObject.getJSONObject(key)
                if (oneReportObject.has(it.key)) {
                    oneReportObject.put(it.value, oneReportObject[it.key])
                    oneReportObject.put("fileName", key)
                    oneReportObject.remove(it.key)
                }
            }
        }
    }
}
