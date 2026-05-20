package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

/**
 * Holds all pre-approval configurations.
 */
data class PreApprovalConfig(
    @field:DecimalMin(value = "0.0", message = "samplingProbability must be >= 0.0")
    @field:DecimalMax(value = "1.0", message = "samplingProbability must be <= 1.0")
    val samplingProbability: Double = 0.0,
)
