package org.dataland.datalandspecificationservice.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.Framework

/**
 * Get the reference for this framework specification.
 */
fun Framework.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/frameworks/${this.id}",
    )

/**
 * Translate the schema of a framework specification.
 */
private fun translateSchema(
    schema: ObjectNode,
    baseUrl: String,
    database: SpecificationDatabase,
) {
    for ((key, value) in schema.fields()) {
        if (value.isObject) {
            translateSchema(value as ObjectNode, baseUrl, database)
        } else {
            assert(value.isTextual)
            val dataPointSpec =
                database.dataPointTypes[value.asText()]
                    ?: throw IllegalArgumentException("Data point type id ${value.asText()} does not exist in the database.")
            val idWithRef = dataPointSpec.getRef(baseUrl)

            val idWithRefNode: ObjectNode = JsonNodeFactory.instance.objectNode()
            idWithRefNode.put("id", idWithRef.id)
            idWithRefNode.put("ref", idWithRef.ref)

            schema.set<ObjectNode>(key, idWithRefNode)
        }
    }
}

/**
 * Convert a framework specification to a DTO.
 */
fun Framework.toDto(
    baseUrl: String,
    database: SpecificationDatabase,
): FrameworkSpecification {
    val newSchema: ObjectNode = this.schema.deepCopy()
    translateSchema(newSchema, baseUrl, database)
    return FrameworkSpecification(
        framework = this.getRef(baseUrl),
        name = this.name,
        businessDefinition = this.businessDefinition,
        schema = ObjectMapper().writeValueAsString(newSchema),
        referencedReportJsonPath = this.referencedReportJsonPath,
    )
}

/**
 * Convert a framework specification to a simplified DTO.
 */
fun Framework.toSimpleDto(baseUrl: String): SimpleFrameworkSpecification =
    SimpleFrameworkSpecification(
        framework = this.getRef(baseUrl),
        name = this.name,
    )

/**
 * A DTO for a framework specification.
 */
data class FrameworkSpecification(
    val framework: IdWithRef,
    val name: String,
    val businessDefinition: String,
    val schema: String,
    val referencedReportJsonPath: String?,
)

/**
 * A simplified DTO for framework specification.
 */
data class SimpleFrameworkSpecification(
    val framework: IdWithRef,
    val name: String,
)
