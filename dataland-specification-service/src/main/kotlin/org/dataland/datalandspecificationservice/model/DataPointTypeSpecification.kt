package org.dataland.datalandspecificationservice.model

import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointType

/**
 * Get the reference for a data point type specification.
 */
fun DataPointType.getRef(baseUrl: String): IdWithRef =
    IdWithRef(
        id = this.id,
        ref = "https://$baseUrl/specifications/data-point-types/${this.id}",
    )

/**
 * Create a data point type specification DTO.
 */
fun DataPointType.toDto(
    baseUrl: String,
    database: SpecificationDatabase,
): DataPointTypeSpecification =
    DataPointTypeSpecification(
        dataPointType = this.getRef(baseUrl),
        name = this.name,
        businessDefinition = this.businessDefinition,
        usedBy =
            database.frameworks.values
                .filter {
                    it.flattenedSchema.any { schemaEntry -> schemaEntry.dataPointId == this.id }
                }.map { it.getRef(baseUrl) },
        dataPointBaseType =
            database.dataPointBaseTypes[this.dataPointBaseTypeId]?.getRef(
                baseUrl,
            ) ?: error("Data point type id ${this.dataPointBaseTypeId} does not exist in the database."),
    )

/**
 * DTO for a data point type specification.
 */
data class DataPointTypeSpecification(
    val dataPointType: IdWithRef,
    val name: String,
    val businessDefinition: String,
    val dataPointBaseType: IdWithRef,
    val usedBy: List<IdWithRef>,
)
