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
        kpiHolder: JSONObject,
        oldKpiName: String,
        newKpiName: String,
        transformation: (O) -> N = {
            require(it is N)
            it
        },
    ) {
        kpiHolder.put(newKpiName, transformation((kpiHolder.getOrJavaNull(oldKpiName) ?: return) as O))
    }

    inline fun <O, reified N> migrateValueFromToAndQueueForRemoval(
        kpiHolder: JSONObject,
        oldKpiName: String,
        newKpiName: String,
        transformation: (O) -> N = {
            require(it is N)
            it
        },
    ) {
        migrateValueFromTo(kpiHolder, oldKpiName, newKpiName, transformation)
        queueFieldForRemoval(kpiHolder, oldKpiName)
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