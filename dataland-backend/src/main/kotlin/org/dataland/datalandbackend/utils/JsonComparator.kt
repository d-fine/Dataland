package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode

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

    private fun isFullyNullObject(node: JsonNode): Boolean =
        node.isNull ||
            node.isObject &&
            node.fields().asSequence().all {
                it.value.isNull
            }

    private fun findNodeDifferences(
        expected: JsonNode,
        actual: JsonNode,
        options: JsonComparisonOptions,
        currentPath: String = "",
        differenceList: MutableList<JsonDiff>,
    ) {
        when {
            expected.isNull && actual.isNull -> {
                // Both nodes are null
            }
            expected.isNull || actual.isNull -> {
                if (options.fullyNullObjectsAreEqualToNull && isFullyNullObject(expected) && isFullyNullObject(actual)) {
                    // Both objects are null-ish
                } else {
                    differenceList.add(JsonDiff(currentPath, expected, actual))
                }
            }
            expected.isObject && actual.isObject -> {
                compareObjects(expected, options, actual, currentPath, differenceList)
            }
            expected.isArray && actual.isArray -> {
                compareArrays(expected, actual, currentPath, options, differenceList)
            }
            expected != actual -> {
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
