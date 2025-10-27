package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object DataPointUtils {
    val objectMapper = ObjectMapper()

    /**
     * Retrieves the data point types in a framework schema
     * @param schema of a framework
     * @return a set of all data point types
     */
    fun getDataPointTypes(schema: String): Set<String> {
        val frameworkTemplate = jacksonObjectMapper().readTree(schema)
        return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
    }

    /**
     * Converts the given data string corresponding to the plain data point format to the
     * associated extended data point format (in which only the value field is set).
     */
    fun convertToExtendedFormat(plainData: String): String {
        val onceSerialized = objectMapper.readValue(plainData, String::class.java)
        val plainValue = objectMapper.readValue(onceSerialized, String::class.java)
        val extendedObject =
            objectMapper.createObjectNode().apply {
                put("value", plainValue)
            }
        return objectMapper.writeValueAsString(objectMapper.writeValueAsString(extendedObject))
    }
}
