package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.text.SimpleDateFormat

object JsonUtils {
    private const val JSON_PATH_SEPARATOR = "."

    /**
     * The object mapper used for testing.
     */
    val defaultObjectMapper: ObjectMapper =
        jacksonObjectMapper()
            .findAndRegisterModules()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)

    /**
     * Return the path separator used in constructing node paths
     */
    fun getPathSeparator(): String = JSON_PATH_SEPARATOR

    /**
     * Get all leaf node field names mapped to their corresponding value from a JSON node.
     * The field names are essentially the leaf JSON paths using the default JSON path child operator "."
     * Leaf null values are ignored.
     *
     * @param node the JSON node
     * @param currentPath the current path
     * @return a mapping of the names of non-empty fields to their respective values
     */
    fun getAllLeafNodesAsMapping(
        node: JsonNode,
        currentPath: String = "",
    ): MutableMap<String, String?> {
        val result = mutableMapOf<String, String?>()

        when {
            node.isValueNode -> {
                val nodeString =
                    if (node.isTextual) {
                        node.textValue()
                    } else if (node.isNull) {
                        null
                    } else {
                        node.toString()
                    }
                result[currentPath] = nodeString
            }

            node.isObject -> {
                node.fields().forEachRemaining { (fieldName, value) ->
                    val newPath = if (currentPath.isEmpty()) fieldName else "$currentPath$JSON_PATH_SEPARATOR$fieldName"
                    result.putAll(getAllLeafNodesAsMapping(value, newPath))
                }
            }

            node.isArray -> {
                node.elements().withIndex().forEachRemaining { (index, element) ->
                    val newPath = if (currentPath.isEmpty()) "$index" else "$currentPath$JSON_PATH_SEPARATOR$index"
                    result.putAll(getAllLeafNodesAsMapping(element, newPath))
                }
            }
        }

        return result
    }

    /**
     * Get all leaf node field names from a JSON node.
     * The field names are essentially the leaf JSON paths using the default JSON path child operator "."
     * Leaf null values are ignored.
     * @param node the JSON node
     * @param currentPath the current path
     * @param ignoreArrays ignores Arrays if set to true
     * @param keepEmptyFields if set to true, nodes with value null will also be extracted
     * @param dropLastFieldName if set to true, the last part of the path is cut off
     * @return a list of leaf node field names
     */
    fun getLeafNodeFieldNames(
        node: JsonNode,
        currentPath: String = "",
        ignoreArrays: Boolean = false,
        keepEmptyFields: Boolean = false,
        dropLastFieldName: Boolean = false,
    ): LinkedHashSet<String> {
        val leafNodeFieldNames = linkedSetOf<String>()

        when {
            node.isValueNode -> {
                if (!node.isNull || keepEmptyFields) {
                    leafNodeFieldNames.add(
                        if (dropLastFieldName) currentPath.substringBeforeLast(JSON_PATH_SEPARATOR) else currentPath,
                    )
                }
            }

            node.isObject -> {
                node.fields().forEachRemaining { (fieldName, value) ->
                    val newPath = if (currentPath.isEmpty()) fieldName else "$currentPath$JSON_PATH_SEPARATOR$fieldName"
                    leafNodeFieldNames.addAll(getLeafNodeFieldNames(value, newPath, ignoreArrays, keepEmptyFields, dropLastFieldName))
                }
            }

            node.isArray && !ignoreArrays -> {
                node.elements().withIndex().forEachRemaining { (index, element) ->
                    val newPath = if (currentPath.isEmpty()) "$index" else "$currentPath$JSON_PATH_SEPARATOR$index"
                    leafNodeFieldNames.addAll(getLeafNodeFieldNames(element, newPath, ignoreArrays, keepEmptyFields, dropLastFieldName))
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
    ): LinkedHashSet<String> = getLeafNodeFieldNames(node, currentPath, true)

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
