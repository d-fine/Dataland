package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.VerificationUtils.flattenSchema

/**
 * A specification for a framework
 */
data class Framework(
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
    val flattenedSchema: List<FrameworkSchemaEntry>
        get() = flattenSchema(schema, "").toList()

    /**
     * Validates the integrity of the framework specification.
     */
    fun validateIntegrity(database: SpecificationDatabase) {
        VerificationUtils.assertValidId(id)
        val dataPointIds: Set<String> = database.dataPointTypes.keys
        val schemaDataPointIds = flattenedSchema.map { it.value }.toSet()
        val missingDataPointIds = schemaDataPointIds - dataPointIds
        check(missingDataPointIds.isEmpty()) { "The following data point ids are missing in the database: $missingDataPointIds" }
    }
}
