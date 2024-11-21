package org.dataland.datalandspecificationservice.model

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointTypeSpecification

/**
 * Get the reference for this data point type specification.
 */
fun DataPointTypeSpecification.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/data-point-types/${this.id}",
    )

/**
 * Convert a data point type specification to a DTO.
 */
fun DataPointTypeSpecification.toDto(
    baseUrl: String,
    database: SpecificationDatabase,
): DataPointTypeSpecificationDto =
    DataPointTypeSpecificationDto(
        dataPointTypeSpecification = this.getRef(baseUrl),
        name = this.name,
        businessDefinition = this.businessDefinition,
        validatedBy = this.validatedBy,
        example = this.example,
        usedBy =
            database.dataPointSpecifications.values
                .filter {
                    it.dataPointTypeId == this.id
                }.map { it.getRef(baseUrl) },
    )

/**
 * DTO for a data point type specification.
 */
data class DataPointTypeSpecificationDto(
    val dataPointTypeSpecification: IdWithRef,
    val name: String,
    val businessDefinition: String,
    val validatedBy: String,
    val example: JsonNode,
    val usedBy: List<IdWithRef>,
)
