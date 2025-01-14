package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.databind.JsonNode

/**
 * A specification for a data point schema.
 */
data class DataPointSchema(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val validatedBy: String,
    val example: JsonNode,
) {
    /**
     * Validates the integrity of the data point schema.
     */
    fun validateIntegrity() {
        VerificationUtils.assertValidId(id)
    }
}
