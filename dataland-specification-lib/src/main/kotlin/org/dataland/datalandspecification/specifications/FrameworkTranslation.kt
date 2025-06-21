package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.specifications.VerificationUtils.flattenSchema

/**
 * Framework specification including its schema and related operations.
 *
 * @property id The unique identifier of the framework.
 * @property schema The JSON schema of the framework as an ObjectNode.
 */
data class FrameworkTranslation(
    val id: String,
    val schema: ObjectNode = JsonNodeFactory.instance.objectNode(),
) {
    /**
     * Returns a flattened list representation of the schema.
     */
    @get:JsonIgnore
    val flattenedSchema: List<FrameworkSchemaEntry>
        get() = flattenSchema(schema, "").toList()

    /**
     * Validates the integrity of the framework specification and
     * checks for duplicate export aliases in the flattened schema
     */
    fun validateIntegrity() {
        val schemaExportAliases = flattenedSchema.mapNotNull { it.value }.toList()
        val duplicatedAliases =
            schemaExportAliases
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
        check(duplicatedAliases.isEmpty()) { "The following exportAliases for $id are duplicates: ${duplicatedAliases.entries}" }
    }
}
