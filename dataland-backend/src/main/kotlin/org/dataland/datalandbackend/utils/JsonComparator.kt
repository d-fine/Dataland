package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import java.math.BigDecimal

/**
 * Compares two JSON nodes and returns a list of differences.
 */
object JsonComparator {
    /**
     * Represents a difference between two JSON nodes.
     */
    data class JsonDiff(
        val path: String,
        val expected: JsonNode?,
        val actual: JsonNode?,
    )

    /**
     * Json Comparison Options
     */
    data class JsonComparisonOptions(
        val ignoredKeys: Set<String> = emptySet(),
        val fullyNullObjectsAreEqualToNull: Boolean = true,
    )

    /**
     * Compares two JSON nodes and returns a list of differences.
     */
    fun compareJson(
        expected: JsonNode,
        actual: JsonNode,
        options: JsonComparisonOptions,
    ): List<JsonDiff> {
        val differences = mutableListOf<JsonDiff>()
        findNodeDifferences(expected, actual, options, differenceList = differences)
        return differences
    }

    /**
     * Checks recursively if a given JSON node is a fully null object (all fields are null or empty).
     * @param node The JSON node to check.
     * @return True if the node is a fully null object, false otherwise.
     */
    fun isFullyNullObject(node: JsonNode): Boolean =
        node.isNull ||
            node.isObject &&
            node.fields().asSequence().all {
                it.value.isNull || it.value.isEmpty || isFullyNullObject(it.value)
            }

    private fun valuesDiffer(
        expected: JsonNode,
        actual: JsonNode,
    ): Boolean {
        if (expected.isNumber && actual.isNumber) {
            return BigDecimal(expected.asText()).compareTo(BigDecimal(actual.asText())) != 0
        }
        return expected != actual
    }

    private fun findNullNodeDifferences(
        expected: JsonNode,
        actual: JsonNode,
        options: JsonComparisonOptions,
        currentPath: String = "",
        differenceList: MutableList<JsonDiff>,
    ) {
        if (expected.isNull && actual.isNull) {
            // Both nodes are null
        } else if (options.fullyNullObjectsAreEqualToNull && isFullyNullObject(expected) && isFullyNullObject(actual)) {
            // Both objects are null-ish
        } else {
            differenceList.add(JsonDiff(currentPath, expected, actual))
        }
    }

    private fun findNodeDifferences(
        expected: JsonNode,
        actual: JsonNode,
        options: JsonComparisonOptions,
        currentPath: String = "",
        differenceList: MutableList<JsonDiff>,
    ) {
        when {
            expected.isNull || actual.isNull -> {
                findNullNodeDifferences(expected, actual, options, currentPath, differenceList)
            }
            expected.isObject && actual.isObject -> {
                compareObjects(expected, options, actual, currentPath, differenceList)
            }
            expected.isArray && actual.isArray -> {
                compareArrays(expected, actual, currentPath, options, differenceList)
            }
            valuesDiffer(expected, actual) -> {
                differenceList.add(JsonDiff(currentPath, expected, actual))
            }
            else -> {
                // The nodes are equal
            }
        }
    }

    private fun compareArrays(
        expected: JsonNode,
        actual: JsonNode,
        currentPath: String,
        options: JsonComparisonOptions,
        differenceList: MutableList<JsonDiff>,
    ) {
        val expectedSize = expected.size()
        val actualSize = actual.size()
        val maxSize = maxOf(expectedSize, actualSize)
        for (i in 0 until maxSize) {
            val newPath = if (currentPath.isEmpty()) "[$i]" else "$currentPath[$i]"
            findNodeDifferences(
                expected.get(i) ?: NullNode.instance,
                actual.get(i) ?: NullNode.instance,
                options,
                newPath,
                differenceList,
            )
        }
    }

    private fun compareObjects(
        expected: JsonNode,
        options: JsonComparisonOptions,
        actual: JsonNode,
        currentPath: String,
        differenceList: MutableList<JsonDiff>,
    ) {
        val expectedFields =
            expected
                .fieldNames()
                .asSequence()
                .filterNot { it in options.ignoredKeys }
                .toSet()
        val actualFields =
            actual
                .fieldNames()
                .asSequence()
                .filterNot { it in options.ignoredKeys }
                .toSet()
        val allFields = expectedFields + actualFields
        for (field in allFields) {
            val newPath = if (currentPath.isEmpty()) field else "$currentPath.$field"
            findNodeDifferences(
                expected[field] ?: NullNode.instance,
                actual[field] ?: NullNode.instance,
                options,
                newPath,
                differenceList,
            )
        }
    }
}
