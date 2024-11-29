package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.database.SpecificationDatabase

/**
 * A specification for a nested schema entry
 */
data class FrameworkSpecificationSchemaEntry(
    val jsonPath: String,
    val dataPointId: String,
)

/**
 * A specification for a framework
 */
data class FrameworkSpecification(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val referencedReportJsonPath: String? = null,
    val schema: ObjectNode = JsonNodeFactory.instance.objectNode(),
) {
    /**
     * A flattened version of the schema
     */
    @get:JsonIgnore
    val flattenedSchema: List<FrameworkSpecificationSchemaEntry>
        get() = flattenSchema(schema, "").toList()

    private fun flattenSchema(
        schema: ObjectNode,
        prefix: String,
    ): Sequence<FrameworkSpecificationSchemaEntry> {
        assert(schema.isObject)
        return sequence {
            schema.fields().forEach { (key, value) ->
                val newPath = "$prefix.$key"
                if (value.isObject) {
                    yieldAll(flattenSchema(value as ObjectNode, newPath))
                } else {
                    assert(value.isTextual)
                    yield(FrameworkSpecificationSchemaEntry(newPath, value.asText()))
                }
            }
        }
    }

    /**
     * Validates the integrity of the framework specification.
     */
    fun validateIntegrity(database: SpecificationDatabase) {
        VerificationUtils.assertValidId(id)
        val dataPointIds : Set<String> = database.dataPointSpecifications.keys
        val schemaDataPointIds = flattenedSchema.map { it.dataPointId }.toSet()
        val missingDataPointIds = schemaDataPointIds - dataPointIds
        check(missingDataPointIds.isEmpty()) { "The following data point ids are missing in the database: $missingDataPointIds" }
    }
}
