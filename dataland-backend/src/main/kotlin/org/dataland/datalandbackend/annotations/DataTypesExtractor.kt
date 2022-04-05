package org.dataland.datalandbackend.annotations

import org.reflections.Reflections

/**
 * Class to extract DataType annotations
 */
class DataTypesExtractor {

    /**
     * Method to list all the permissible data types
     * @return list of all permissible data types
     */
    fun getAllDataTypes(): List<String> {
        val reflections = Reflections("org.dataland.datalandbackend.model")
        return reflections.getTypesAnnotatedWith(DataType::class.java).map { it.simpleName }
    }
}
