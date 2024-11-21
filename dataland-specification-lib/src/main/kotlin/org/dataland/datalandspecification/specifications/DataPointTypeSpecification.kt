package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.databind.JsonNode

/**
 * A specification for a data point type.
 */
data class DataPointTypeSpecification(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val validatedBy: String,
    val example: JsonNode,
) {
    /**
     * Validates the integrity of the data point type specification.
     */
    fun validateIntegrity() {
        VerificationUtils.assertValidId(id)
    }
}
