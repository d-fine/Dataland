package org.dataland.datalandbackend.model.p2p.categories.electricityGeneration.subcategories

import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the subcategory "Technology" belonging to the category "Electricity generation" of the p2p framework.
*/
data class P2pElectricityGenerationTechnology(
    val electricityMixEmissionsInCorrespondingUnit: BigDecimal? = null,
    val shareOfRenewableElectricityInPercent: BigDecimal? = null,
    val naturalGasPhaseOut: LocalDate? = null,
    val coalPhaseOut: LocalDate? = null,
    val storageCapacityExpansionInPercent: BigDecimal? = null,
)
