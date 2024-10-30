package org.dataland.datalandspecification.specifications

import org.dataland.datalandspecification.database.SpecificationDatabase

/**
 * A specification for a data point.
 */
data class DataPointSpecification(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val dataPointTypeId: String,
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
