package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.JsonNode

object JsonUtils {
    private const val JSON_PATH_SEPARATOR = "."

    /**
     * Get all leaf node field names from a JSON node.
     * The field names are essentially the leaves' JSON paths using the default JSON path child operator "."
     * Leaf null values are ignored.
     * @param node The JSON node
     * @param currentPath The current path
     * @param ignoreArrays ignores Arrays if set to true
     * @return A list of leaf node field names
     */
    fun getLeafNodeFieldNames(
        node: JsonNode,
        currentPath: String = "",
        ignoreArrays: Boolean = false,
    ): List<String> {
        val leafNodeFieldNames = mutableListOf<String>()

        when {
            node.isValueNode -> {
                if (!node.isNull) {
                    leafNodeFieldNames.add(currentPath)
                }
            }

            node.isObject -> {
                node.fields().forEachRemaining { (fieldName, value) ->
                    val newPath = if (currentPath.isEmpty()) fieldName else "$currentPath$JSON_PATH_SEPARATOR$fieldName"
                    leafNodeFieldNames.addAll(getLeafNodeFieldNames(value, newPath, ignoreArrays))
                }
            }

            node.isArray && !ignoreArrays -> {
                node.elements().withIndex().forEachRemaining { (index, element) ->
                    val newPath = if (currentPath.isEmpty()) "$index" else "$currentPath$JSON_PATH_SEPARATOR$index"
                    leafNodeFieldNames.addAll(getLeafNodeFieldNames(element, newPath, ignoreArrays))
                }
            }
        }
        return leafNodeFieldNames
    }

    /**
     * Get all leaf node field names from a JSON node ignoring ArrayNodes.
     * Function is used in dataland-data-exporter service
     * @param node The JSON node
     * @param currentPath The current path
     * @return A list of non-array leaf node field names
     */
    fun getNonArrayLeafNodeFieldNames(
        node: JsonNode,
        currentPath: String = "",
    ): List<String> = getLeafNodeFieldNames(node, currentPath, true)

    /**
     * Get the string value of the JSON node identified by the JSON path.
     * First, the function splits the [jsonPath] at the defined [JSON_PATH_SEPARATOR] and then follows this path to the
     * leaf node. If the currentNode is an array, the path is an array index which is why we have to transform it to Int
     * in order for the get method to work properly.
     * @param jsonNode The JSON node
     * @param jsonPath The JSON path identifying the value
     * @return The string representation of the value
     */
    fun getValueFromJsonNodeByPath(
        jsonNode: JsonNode,
        jsonPath: String,
    ): String {
        var currentNode = jsonNode
        jsonPath.split(JSON_PATH_SEPARATOR).forEach { path ->
            currentNode = if (currentNode.isArray) {
                currentNode.get(path.toInt())
            } else {
                currentNode.get(path)
            } ?: return ""
        }
        return if (currentNode.isNull) {
            ""
        } else if (currentNode.isTextual) {
            currentNode.textValue()
        } else {
            currentNode.toString()
        }
    }
}
