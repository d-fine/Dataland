package org.dataland.datalandbackend.frameworks.nuclearandgas.custom

import java.math.BigDecimal

/**
 * --- API model ---
 * Custom Child Component for the EU-Taxonomy Nuclear and Gas Templates "Aligned (Denominator)", "Aligned (Numerator)",
 * "Eligible but not-aligned"
 */
data class NuclearAndGasEnvironmentalObjective(
    val mitigationAndAdaptation: BigDecimal? = null,
    val mitigation: BigDecimal? = null,
    val adaptation: BigDecimal? = null,
)
