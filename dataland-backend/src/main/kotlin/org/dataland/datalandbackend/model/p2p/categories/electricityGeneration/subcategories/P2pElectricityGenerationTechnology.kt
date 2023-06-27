package org.dataland.datalandbackend.model.p2p.categories.electricityGeneration.subcategories

import java.math.BigDecimal
import java.time.LocalDate

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the technology of the electricity generation sector
*/
data class P2pElectricityGenerationTechnology(
    val electricityMixEmissions: BigDecimal?,

    val shareOfRenewableElectricity: BigDecimal?,

    val naturalGasPhaseOut: LocalDate?,

    val coalPhaseOut: LocalDate?,

    val storageCapacityExpansion: BigDecimal?,
)
