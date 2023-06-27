package org.dataland.datalandbackend.model.p2p.categories.steel.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the technology of the steel sector
*/
data class P2pSteelTechnology(
    val blastFurnacePhaseOut: BigDecimal?,

    val lowCarbonSteelScaleUp: BigDecimal?,
)
