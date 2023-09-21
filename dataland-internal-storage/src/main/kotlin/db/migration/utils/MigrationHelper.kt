package db.migration.utils

import org.json.JSONObject
import kotlin.reflect.jvm.internal.impl.builtins.functions.FunctionTypeKind.KFunction

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

    inline fun <reified T> migrateDataPointValue(
        dataPointHolder: JSONObject,
        dataPointName: String,
        transformation: (T) -> T = { it },
    ) {
        val dataPointObject = (dataPointHolder.getOrJavaNull(dataPointName) ?: return) as JSONObject
        migrateValue(dataPointObject, "value", transformation)
    }

//    inline fun <O, reified N> migrateDataPointValueFromTo(
//        kpiHolder: JSONObject,
//        oldDataPointName: String,
//        newDataPointName: String,
//        transformation: (O) -> N = {
//            require(it is N)
//            it
//        },
//    ) {
//        kpiHolder.put(newDataPointName, transformation((kpiHolder.getOrJavaNull(oldDataPointName) ?: return) as O))
//    }
}