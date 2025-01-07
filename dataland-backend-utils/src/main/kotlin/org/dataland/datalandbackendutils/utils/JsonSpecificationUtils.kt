package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException

/**
 * A leaf node in a JSON specification, containing the data point ID, the JSON path, and the content.
 */
data class JsonSpecificationLeaf(
    val dataPointId: String,
    val jsonPath: String,
    val content: JsonNode,
)

/**
 * Utilities for working with JSON framework specifications.
 */
object JsonSpecificationUtils {
    private fun isTerminalNode(node: ObjectNode): Boolean = node.fieldNames().asSequence().toSet() == setOf("id", "ref")

    /**
     * Hydrates a JSON specification with data from a data function. During hydration, leaf nodes of the
     * specification are replaced with data requested from the data function.
     * @param jsonSpecification The JSON specification to hydrate (from the specification service)
     * @param dataFunction A function that takes a dataPointId and returns the data for that key
     * @return The hydrated JSON specification (i.e., the constructed dataset)
     */
    fun hydrateJsonSpecification(
        jsonSpecification: ObjectNode,
        dataFunction: (String) -> JsonNode?,
    ): JsonNode? = hydrateJsonSpecificationRecursive(jsonSpecification, dataFunction)

    private fun hydrateJsonSpecificationRecursive(
        currentSpecificationNode: ObjectNode,
        dataFunction: (String) -> JsonNode?,
    ): JsonNode? =
        if (isTerminalNode(currentSpecificationNode)) {
            val dataPointId = currentSpecificationNode.get("id").asText()
            dataFunction(dataPointId)
        } else {
            val returnNode = JsonNodeFactory.instance.objectNode()
            var allNull = true
            for ((fieldName, childSpecificationNode) in currentSpecificationNode.fields()) {
                require(childSpecificationNode is ObjectNode) {
                    "Specification node must be an object node"
                }
                val hydratedChild = hydrateJsonSpecificationRecursive(childSpecificationNode, dataFunction)
                if (hydratedChild != null) {
                    allNull = false
                    returnNode.set<JsonNode>(fieldName, hydratedChild)
                }
            }
            if (allNull) null else returnNode
        }

    /**
     * Dehydrates a JSON specification into a map of dataPointIds to data. During dehydration, the data
     * from the data object is extracted and stored in a map. Unknown keys cause an exception.
     * @param jsonSpecification The JSON specification to dehydrate (from the specification service)
     * @param dataObject The data object to dehydrate
     * @return A map of dataPointIds to data
     */
    fun dehydrateJsonSpecification(
        jsonSpecification: ObjectNode,
        dataObject: ObjectNode,
    ): Map<String, JsonSpecificationLeaf> {
        val returnMap = mutableMapOf<String, JsonSpecificationLeaf>()
        dehydrateJsonSpecificationRecursive("", jsonSpecification, dataObject, returnMap)
        return returnMap
    }

    private fun dehydrateJsonSpecificationRecursive(
        currentPath: String,
        currentSpecificationNode: ObjectNode,
        currentDataNode: JsonNode,
        dataMap: MutableMap<String, JsonSpecificationLeaf>,
    ) {
        if (isTerminalNode(currentSpecificationNode)) {
            val dataPointId = currentSpecificationNode.get("id").asText()
            dataMap[dataPointId] = JsonSpecificationLeaf(dataPointId, currentPath, currentDataNode)
        } else {
            if (currentDataNode.isNull) {
                return
            }
            if (currentDataNode !is ObjectNode) {
                throw InvalidInputApiException(
                    summary = "Data object does not match specification",
                    "Unexpected leaf at JSON path: $currentPath",
                )
            }
            for ((fieldName, jsonNode) in currentDataNode.fields()) {
                if (jsonNode.isNull) {
                    continue
                }
                val matchingSpecificationNode =
                    currentSpecificationNode.get(fieldName)
                        ?: throw InvalidInputApiException(
                            summary = "Data object does not match specification",
                            "Unexpected field at JSON path: $currentPath.$fieldName",
                        )
                require(matchingSpecificationNode is ObjectNode) {
                    "Specification node must be an object node"
                }
                dehydrateJsonSpecificationRecursive(
                    currentPath = "$currentPath.$fieldName",
                    currentSpecificationNode = matchingSpecificationNode,
                    currentDataNode = jsonNode,
                    dataMap = dataMap,
                )
            }
        }
    }
}
