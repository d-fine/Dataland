package org.dataland.datalandbackend.annotations

import org.reflections.Reflections
import org.slf4j.LoggerFactory

/**
 * Class to extract DataType annotations
 */
class DataTypesExtractor {
    private val logger = LoggerFactory.getLogger(javaClass)
    /**
     * Method to list all the permissible data types
     * @return list of all permissible data types
     */
    fun getAllDataTypes(): List<String> {
        val reflections = Reflections("org.dataland.datalandbackend.model")
        val allDataTypes = reflections.getTypesAnnotatedWith(DataType::class.java).map { it.simpleName }
        logger.info("Searching for known Datatypes. Datatypes found: $allDataTypes")
        return allDataTypes
    }
}
