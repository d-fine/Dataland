package org.dataland.datalandspecificationservice.model

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointBaseType

/**
 * Get the reference for this data point base type specification.
 */
fun DataPointBaseType.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/data-point-base-types/${this.id}",
    )

/**
 * Convert a data point base type to the corresponding specification DTO.
 */
fun DataPointBaseType.toDto(
    baseUrl: String,
    database: SpecificationDatabase,
): DataPointBaseTypeSpecification =
    DataPointBaseTypeSpecification(
        dataPointBaseType = this.getRef(baseUrl),
        name = this.name,
        businessDefinition = this.businessDefinition,
        validatedBy = this.validatedBy,
        example = this.example,
        usedBy =
            database.dataPointTypes.values
                .filter {
                    it.dataPointBaseTypeId == this.id
                }.map { it.getRef(baseUrl) },
    )

/**
 * DTO for a data point type specification.
 */
data class DataPointBaseTypeSpecification(
    val dataPointBaseType: IdWithRef,
    val name: String,
    val businessDefinition: String,
    val validatedBy: String,
    val example: JsonNode,
    val usedBy: List<IdWithRef>,
)
