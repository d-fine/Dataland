package org.dataland.datalandspecificationservice.model

import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointSpecification

/**
 * Get the reference for a data point specification.
 */
fun DataPointSpecification.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/data-points/${this.id}",
    )

/**
 * Convert a data point specification to a DTO.
 */
fun DataPointSpecification.toDto(
    baseUrl: String,
    database: SpecificationDatabase,
): DataPointSpecificationDto =
    DataPointSpecificationDto(
        dataPointSpecification = this.getRef(baseUrl),
        name = this.name,
        businessDefinition = this.businessDefinition,
        usedBy =
            database.frameworkSpecifications.values
                .filter {
                    it.flattenedSchema.any { schemaEntry -> schemaEntry.dataPointId == this.id }
                }.map { it.getRef(baseUrl) },
        validatedBy =
            database.dataPointTypeSpecifications[this.dataPointTypeId]?.getRef(
                baseUrl,
            ) ?: error("Data point type id ${this.dataPointTypeId} does not exist in the database."),
    )

/**
 * DTO for a data point specification.
 */
data class DataPointSpecificationDto(
    val dataPointSpecification: IdWithRef,
    val name: String,
    val businessDefinition: String,
    val validatedBy: IdWithRef,
    val usedBy: List<IdWithRef>,
)
