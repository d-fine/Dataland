package org.dataland.datalandbackend.model.p2p.categories.electricityGeneration.subcategories

import java.math.BigDecimal
import java.time.LocalDate

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the technology of the electricity generation sector
*/
data class P2pElectricityGenerationTechnology(
    val electricityMixEmissions: BigDecimal? = null,

    val shareOfRenewableElectricityInPercent: BigDecimal? = null,

    val naturalGasPhaseOut: LocalDate? = null,

    val coalPhaseOut: LocalDate? = null,

    val storageCapacityExpansionInPercent: BigDecimal? = null,
)
