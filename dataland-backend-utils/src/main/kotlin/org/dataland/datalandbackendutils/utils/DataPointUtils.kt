package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object DataPointUtils {
    /**
     * Retrieves the data point types in a framework schema
     * @param schema of a framework
     * @return a set of all data point types
     */
    fun getDataPointTypes(schema: String): Set<String> {
        val frameworkTemplate = jacksonObjectMapper().readTree(schema) as ObjectNode
        return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
    }
}
