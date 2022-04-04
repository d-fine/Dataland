package org.dataland.datalandbackend.annotations

import org.reflections.Reflections

/**
 * Class to process custom annotations
 */
class AnnotationProcessor {

    /**
     * Method to list all the permissible data types
     * @return list of all permissible data types
     */
    fun getAllDataTypes(): List<String> {
        val dataTypes: MutableList<String> = mutableListOf()
        val reflections = Reflections("org.dataland.datalandbackend.model")
        val allAnnClasses = reflections.getTypesAnnotatedWith(DataTypeAnnotation::class.java)
        for (clazz in allAnnClasses) {
            val datatype = clazz.simpleName
            dataTypes.add(datatype)
        }
        return dataTypes
    }
}
