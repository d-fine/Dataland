package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.specifications.VerificationUtils.flattenSchema

/**
 * A specification for a framework
 */
data class FrameworkTranslation(
    val id: String,
    val schema: ObjectNode = JsonNodeFactory.instance.objectNode(),
    val aliasExport: String,
) {
    /**
     * A flattened version of the schema
     */
    @get:JsonIgnore
    val flattenedSchema: List<FrameworkSchemaEntry>
        get() = flattenSchema(schema, "").toList()

    /**
     * Validates the integrity of the framework specification.
     */
    fun validateIntegrity() {
        val schemaExportAliases = flattenedSchema.map { it.value }.toList()
        val duplicatedAliases =
            schemaExportAliases.groupingBy { it }.eachCount().filter { it.value > 1 }
        check(duplicatedAliases.isEmpty()) { "The following exportAliases are duplicates: $duplicatedAliases" }
    }
}
