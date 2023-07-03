package org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the energy of the automotive sector
*/
data class P2pAutomotiveEnergy(
    val productionSiteEnergyConsumption: BigDecimal? = null,

    val energyMix: BigDecimal? = null,
)
