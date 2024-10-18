package org.dataland.datalandbackend.frameworks.nuclearandgas.custom

import java.math.BigDecimal

data class NuclearAndGasEnvironmentalObjective(
    val mitigationAndAdaptation: BigDecimal? = null,
    val mitigation: BigDecimal? = null,
    val adaptation: BigDecimal? = null,
)
