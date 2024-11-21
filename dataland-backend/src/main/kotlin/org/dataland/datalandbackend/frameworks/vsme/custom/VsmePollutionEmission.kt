package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.sme.ReleaseMedium
import java.math.BigDecimal

/**
 * --- API model ---
 * Pollution emission class for vsme framework
 */
data class VsmePollutionEmission(
    @field:JsonProperty(required = true)
    val pollutionType: String,
    val emissionInKilograms: BigDecimal? = null,
    @field:JsonProperty(required = true)
    val releaseMedium: ReleaseMedium,
)
