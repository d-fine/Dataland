package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.JsonNode

object JsonUtils {
    private const val JSON_PATH_SEPARATOR = "."

    /**
     * Gets all leaf node field names from a JSON node.
     * The field names are essentially the leaves' JSON paths using the default JSON path child operator "."
     * Arrays and leaf null values are ignored.
     * @param node The JSON node
     * @param currentPath The current path
     * @return A list of leaf node field names
     */
    fun getNonArrayLeafNodeFieldNames(
        node: JsonNode,
        currentPath: String = "",
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
                    leafNodeFieldNames.addAll(getNonArrayLeafNodeFieldNames(value, newPath))
                }
            }
        }
        return leafNodeFieldNames
    }

    /**
     * Gets the string value of the JSON node identified by the JSON path.
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
            currentNode = currentNode.get(path) ?: return ""
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
