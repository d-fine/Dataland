package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.databind.JsonNode

/**
 * A specification for a data point base type.
 */
data class DataPointBaseType(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val validatedBy: String,
    val example: JsonNode,
) {
    /**
     * Validates the integrity of the data point base type.
     */
    fun validateIntegrity() {
        VerificationUtils.assertValidId(id)
    }
}
