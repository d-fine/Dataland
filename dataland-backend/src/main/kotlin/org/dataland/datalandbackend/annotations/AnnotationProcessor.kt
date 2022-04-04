package org.dataland.datalandbackend.annotations

import org.reflections.Reflections

class AnnotationProcessor {

    fun getAllDataTypes(): List<String> {
        val dataTypes: MutableList<String> = mutableListOf()
        val reflections = Reflections("org.dataland.datalandbackend.model")
        val allAnnClasses = reflections.getTypesAnnotatedWith(DataTypeAnnotation::class.java)
        for (cl in allAnnClasses) {
            val datatype = cl.simpleName
            dataTypes.add(datatype)
        }
        return dataTypes
    }
}
