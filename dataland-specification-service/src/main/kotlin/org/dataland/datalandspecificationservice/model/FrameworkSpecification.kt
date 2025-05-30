package org.dataland.datalandspecificationservice.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.Framework
import org.dataland.datalandspecification.specifications.FrameworkTranslation

/**
 * Get the reference for this framework specification.
 */
fun Framework.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/frameworks/${this.id}",
    )

/**
 * Get the reference and an alias for this framework specification.
 */
fun FrameworkTranslation.getRefAndAlias(baseUrl: String): IdWithRefAndAlias {
    val translationFile = "resources/specifications/translations/${this.id}.json"
    val translation =
        try {
            val resource = this::class.java.classLoader.getResourceAsStream(translationFile)
            if (resource != null) {
                val objectMapper = ObjectMapper()
                objectMapper.readTree(resource)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error while reading translation file $translationFile: ${e.message}")
            null
        }
    val alias = translation?.get(this.id)?.asText() ?: null

    return IdWithRefAndAlias(
        id = this.id,
        ref = "https://$baseUrl/specifications/frameworks/${this.id}",
        aliasExport = alias,
    )
}

/**
 * Translate the schema of a framework specification.
 */
private fun translateSchema(
    schema: ObjectNode,
    baseUrl: String,
    database: SpecificationDatabase,
    framework: String,
) {
    val frameworkSpec = database.translations[framework]?.getRefAndAlias(baseUrl)
    for ((key, value) in schema.fields()) {
        if (value.isObject) {
            translateSchema(value as ObjectNode, baseUrl, database, framework)
        } else {
            assert(value.isTextual)
            val dataPointSpec =
                database.dataPointTypes[value.asText()]
                    ?: throw IllegalArgumentException("Data point type id ${value.asText()} does not exist in the database dataPointTypes.")
            val idWithRef = dataPointSpec.getRef(baseUrl)

            val idWithRefNode: ObjectNode = JsonNodeFactory.instance.objectNode()
            idWithRefNode.put("id", idWithRef.id)
            idWithRefNode.put("ref", idWithRef.ref)
            idWithRefNode.put("aliasExport", frameworkSpec?.aliasExport ?: dataPointSpec.name)

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
    val framework = this.id
    translateSchema(newSchema, baseUrl, database, framework)
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
