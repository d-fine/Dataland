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
    fun queueFieldForRemoval(fieldHolder: JSONObject, fieldName: String) {
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
        val referencedReportsObject = parentCategoryOfReferencedReports
            .getOrJavaNull("referencedReports") ?: return
        referencedReportsObject as JSONObject
        iterateThroughReferencedReports(referencedReportsObject, fieldNamesToMigrate)
    }

    /**
     * Iterates through all referenced reports to migrate reports
     */
    private fun iterateThroughReferencedReports(
        referencedReportsObject: JSONObject,
        fieldNamesToMigrate: Map<String,
            String,>,
    ) {
        for (key in referencedReportsObject.keys()) {
            fieldNamesToMigrate.forEach {
                val oneReportObject = referencedReportsObject.getJSONObject(key)
                if (oneReportObject.has(it.key)) {
                    oneReportObject.put(it.value, oneReportObject.get(it.key))
                    oneReportObject.put("fileName", key)
                    oneReportObject.remove(it.key)
                }
            }
        }
    }
}
