package db.migration.utils

import org.json.JSONObject

class MigrationHelper {
    private data class FieldToRemove(
        val fieldHolder: JSONObject,
        val fieldName: String,
    )

    private val fieldsToRemove = mutableListOf<FieldToRemove>()

    fun queueFieldForRemoval(fieldHolder: JSONObject, fieldName: String) {
        fieldsToRemove.add(FieldToRemove(fieldHolder, fieldName))
    }

    fun removeQueuedFields() {
        fieldsToRemove.forEach {
            it.fieldHolder.remove(it.fieldName)
        }
        fieldsToRemove.clear()
    }

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

    inline fun <reified T> migrateValue(
        fieldHolder: JSONObject,
        fieldName: String,
        transformation: (T) -> T = { it },
    ) {
        migrateValueFromTo(fieldHolder, fieldName, fieldName, transformation)
    }

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
}