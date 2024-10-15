package org.dataland.datalandbackend.frameworks.nuclearandgas.custom

import java.math.BigDecimal

data class NuclearAndGasEnvironmentalObjective(
    val mitigationAndAdaption: BigDecimal? = null,
    val mitigation: BigDecimal? = null,
    val adaption: BigDecimal? = null,
)
