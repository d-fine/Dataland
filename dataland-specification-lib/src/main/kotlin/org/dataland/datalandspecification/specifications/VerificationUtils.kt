package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * A specification for a nested schema entry
 */
data class FrameworkSchemaEntry(
    val jsonPath: String,
    val value: String?,
)

object VerificationUtils {
    private val validIdRegex = Regex("[a-zA-Z0-9_\\-]{1,255}")

    /**
     * Asserts that the given id is valid.
     */
    fun assertValidId(id: String) {
        require(validIdRegex.matches(id)) {
            "Invalid id: $id. Id must have a length between 1 and 255 characters " +
                "and only contain letters, numbers, underscores,and hyphens."
        }
    }

    /**
     * Flattens schema to create frameworkschema entries
     */
    fun flattenSchema(
        schema: ObjectNode,
        prefix: String,
    ): Sequence<FrameworkSchemaEntry> {
        assert(schema.isObject)
        return sequence {
            schema.fields().forEach { (key, value) ->
                val newPath = "$prefix.$key"
                if (value.isObject) {
                    yieldAll(flattenSchema(value as ObjectNode, newPath))
                } else if (value.isNull) {
                    yield(FrameworkSchemaEntry(newPath, null))
                } else if (value.isTextual) {
                    yield(FrameworkSchemaEntry(newPath, value.asText()))
                } else {
                    throw IllegalArgumentException("Unsupported element type")
                }
            }
        }
    }
}
