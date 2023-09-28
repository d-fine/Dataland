package org.dataland.datalandbackend.model.p2p.categories.cement.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the energy of the cement sector
*/
data class P2pCementEnergy(
    val energyMixInPercent: BigDecimal? = null,

    val fuelMixInPercent: BigDecimal? = null,

    val thermalEnergyEfficiencyInPercent: BigDecimal? = null,

    val compositionOfThermalInputInPercent: BigDecimal? = null,
)
