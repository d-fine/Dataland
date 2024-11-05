package org.dataland.datalandspecification.specifications

import org.dataland.datalandspecification.database.SpecificationDatabase

/**
 * A specification for a data point.
 * @param id The unique identifier of the data point specification.
 * @param name The name of the data point specification.
 * @param businessDefinition The business definition of the data point specification.
 * @param dataPointTypeId The unique identifier of the data point type specification.
 * @param frameworkOwnership The "owning" framework of the data point specification.
 *  Used by the framework toolbox to clean-up unused data points.
 */
data class DataPointSpecification(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val dataPointTypeId: String,
    val frameworkOwnership: String? = null,
) {
    /**
     * Validates the integrity of the data point specification.
     */
    fun validateIntegrity(database: SpecificationDatabase) {
        VerificationUtils.assertValidId(id)
        if (!database.dataPointTypeSpecifications.containsKey(dataPointTypeId)) {
            throw IllegalArgumentException("Data point type id $dataPointTypeId does not exist in the database.")
        }
    }
}
