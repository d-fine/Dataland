package org.dataland.datalandspecification.specifications

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
    val schema: ObjectNode = JsonNodeFactory.instance.objectNode(),
) {
    /**
     * A flattened version of the schema
     */
    val flattenedSchema: List<FrameworkSpecificationSchemaEntry>
        get() = flattenSchema(schema, "").toList()

    fun setSchemaEntry(
        jsonPath: String,
        dataPointId: String,
    ) {
        val path = jsonPath.split(".")
        var currentNode = schema
        for (pathSegment in path) {
            if (!currentNode.has(pathSegment)) {
                currentNode.putObject(pathSegment)
            }
            currentNode = currentNode.get(pathSegment) as ObjectNode
        }
        currentNode.put(path.last(), dataPointId)
    }

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
        val dataPointIds = database.dataPointSpecifications.keys
        val schemaDataPointIds = flattenedSchema.map { it.dataPointId }.toSet()
        val missingDataPointIds = schemaDataPointIds - dataPointIds
        if (missingDataPointIds.isNotEmpty()) {
            throw IllegalStateException(
                "The following data point ids " +
                    "are missing in the database: $missingDataPointIds",
            )
        }
    }
}
