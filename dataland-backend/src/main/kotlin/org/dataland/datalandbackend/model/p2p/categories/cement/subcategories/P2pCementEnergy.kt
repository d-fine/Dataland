package org.dataland.datalandbackend.model.p2p.categories.cement.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the energy of the cement sector
*/
data class P2pCementEnergy(
    val energyMix: BigDecimal? = null,

    val fuelMix: BigDecimal? = null,

    val thermalEnergyEfficiency: BigDecimal? = null,

    val compositionOfThermalInput: BigDecimal? = null,
)
