package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode

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
     * Compares two JSON nodes and returns a list of differences.
     */
    fun compareJson(
        expected: JsonNode?,
        actual: JsonNode?,
        ignoredKeys: Set<String>,
    ): List<JsonDiff> {
        val differences = mutableListOf<JsonDiff>()
        findNodeDifferences(expected, actual, ignoredKeys, differenceList = differences)
        return differences
    }

    private fun findNodeDifferences(
        expected: JsonNode?,
        actual: JsonNode?,
        ignoredKeys: Set<String>,
        currentPath: String = "",
        differenceList: MutableList<JsonDiff>,
    ) {
        if (expected == null && actual == null) {
            return
        }

        if (expected == null || actual == null) {
            differenceList.add(JsonDiff(currentPath, expected, actual))
            return
        }

        when {
            expected.isObject && actual.isObject -> {
                compareObjects(expected, ignoredKeys, actual, currentPath, differenceList)
            }
            expected.isArray && actual.isArray -> {
                compareArrays(expected, actual, currentPath, ignoredKeys, differenceList)
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
        ignoredKeys: Set<String>,
        differenceList: MutableList<JsonDiff>,
    ) {
        val expectedSize = expected.size()
        val actualSize = actual.size()
        val maxSize = maxOf(expectedSize, actualSize)
        for (i in 0 until maxSize) {
            val newPath = if (currentPath.isEmpty()) "[$i]" else "$currentPath[$i]"
            findNodeDifferences(
                expected.get(i),
                actual.get(i),
                ignoredKeys,
                newPath,
                differenceList,
            )
        }
    }

    private fun compareObjects(
        expected: JsonNode,
        ignoredKeys: Set<String>,
        actual: JsonNode,
        currentPath: String,
        differenceList: MutableList<JsonDiff>,
    ) {
        val expectedFields =
            expected
                .fieldNames()
                .asSequence()
                .filterNot { it in ignoredKeys }
                .toSet()
        val actualFields =
            actual
                .fieldNames()
                .asSequence()
                .filterNot { it in ignoredKeys }
                .toSet()
        val allFields = expectedFields + actualFields
        for (field in allFields) {
            if (field in ignoredKeys) {
                continue
            }
            val newPath = if (currentPath.isEmpty()) field else "$currentPath.$field"
            findNodeDifferences(
                expected[field],
                actual[field],
                ignoredKeys,
                newPath,
                differenceList,
            )
        }
    }
}
