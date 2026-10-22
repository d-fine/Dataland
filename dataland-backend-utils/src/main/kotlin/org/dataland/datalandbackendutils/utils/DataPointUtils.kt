package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper

object DataPointUtils {
    /**
     * Retrieves the data point types in a framework schema
     * @param schema of a framework
     * @return a set of all data point types
     */
    fun getDataPointTypes(schema: String): Set<String> {
        val frameworkTemplate = defaultObjectMapper.readTree(schema)
        return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
    }

    /**
     * Converts the given data string corresponding to the plain data point format to the
     * associated extended data point format (in which only the value field is set).
     */
    fun convertToExtendedFormat(plainData: String): String {
        val onceSerialized = defaultObjectMapper.readValue(plainData, String::class.java)
        val plainValue = defaultObjectMapper.readValue(onceSerialized, String::class.java)
        val extendedObject =
            defaultObjectMapper.createObjectNode().apply {
                put("value", plainValue)
            }
        return defaultObjectMapper.writeValueAsString(defaultObjectMapper.writeValueAsString(extendedObject))
    }
}
