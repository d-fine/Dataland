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
     * Takes a specification and a corresponding data object and creates a map of dataPointTypes (from [jsonSpecification])
     * to the corresponding data (from [dataObject]). Nodes in the data object not present in the specification cause an exception.
     * @param jsonSpecification The JSON specification to dehydrate (from the specification service)
     * @param dataObject The data object to dehydrate
     * @return A map of data point types to their corresponding data
     */
    fun dehydrateJsonSpecification(
        jsonSpecification: ObjectNode,
        dataObject: ObjectNode,
    ): Map<String, JsonSpecificationLeaf> = dehydrateJsonSpecificationRecursive("", jsonSpecification, dataObject)

    /**
     * Recursively go through the provided data object and return a map of data point types to the corresponding extracted data.
     * @param currentPath The current path in the JSON specification
     * @param currentSpecificationNode The JSON node from the specification corresponding to [currentPath]
     * @param currentDataNode The data object to dehydrate corresponding to [currentPath]
     * @return A map of data point types to their corresponding data
     */
    private fun dehydrateJsonSpecificationRecursive(
        currentPath: String,
        currentSpecificationNode: ObjectNode,
        currentDataNode: JsonNode,
    ): Map<String, JsonSpecificationLeaf> {
        val dataMap = mutableMapOf<String, JsonSpecificationLeaf>()
        if (isTerminalNode(currentSpecificationNode)) {
            val dataPointId = currentSpecificationNode.get("id").asText()
            dataMap[dataPointId] = JsonSpecificationLeaf(dataPointId, currentPath, currentDataNode)
        } else {
            if (currentDataNode.isNull) {
                return dataMap
            }
            if (currentDataNode !is ObjectNode) {
                throw InvalidInputApiException(
                    summary = "Data object does not match specification",
                    "Unexpected leaf at JSON path: $currentPath",
                )
            }
            for ((fieldName, jsonNode) in currentDataNode.fields()) {
                dataMap.putAll(processNode(fieldName, jsonNode, currentPath, currentSpecificationNode))
            }
        }
        return dataMap
    }

    private fun processNode(
        fieldName: String,
        jsonNode: JsonNode,
        currentPath: String,
        currentSpecificationNode: ObjectNode,
    ): Map<String, JsonSpecificationLeaf> {
        val dataMap = mutableMapOf<String, JsonSpecificationLeaf>()
        if (!jsonNode.isNull) {
            val upcomingPath = if (currentPath.isEmpty()) fieldName else "$currentPath.$fieldName"
            val matchingSpecificationNode =
                currentSpecificationNode.get(fieldName)
                    ?: throw InvalidInputApiException(
                        summary = "Data object does not match specification",
                        "Unexpected field at JSON path: $upcomingPath",
                    )
            require(matchingSpecificationNode is ObjectNode) {
                "Specification node must be an object node"
            }
            dataMap.putAll(
                dehydrateJsonSpecificationRecursive(
                    currentPath = upcomingPath,
                    currentSpecificationNode = matchingSpecificationNode,
                    currentDataNode = jsonNode,
                ),
            )
        }
        return dataMap
    }
}
