package org.dataland.datalandspecificationservice.model

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointSchema

/**
 * Get the reference for this data point type specification.
 */
fun DataPointSchema.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/data-point-types/${this.id}",
    )

/**
 * Convert a data point type specification to a DTO.
 */
fun DataPointSchema.toDto(
    baseUrl: String,
    database: SpecificationDatabase,
): DataPointSchemaDto =
    DataPointSchemaDto(
        dataPointSchema = this.getRef(baseUrl),
        name = this.name,
        businessDefinition = this.businessDefinition,
        validatedBy = this.validatedBy,
        example = this.example,
        usedBy =
            database.dataPointSpecifications.values
                .filter {
                    it.dataPointSchemaId == this.id
                }.map { it.getRef(baseUrl) },
    )

/**
 * DTO for a data point type specification.
 */
data class DataPointSchemaDto(
    val dataPointSchema: IdWithRef,
    val name: String,
    val businessDefinition: String,
    val validatedBy: String,
    val example: JsonNode,
    val usedBy: List<IdWithRef>,
)
