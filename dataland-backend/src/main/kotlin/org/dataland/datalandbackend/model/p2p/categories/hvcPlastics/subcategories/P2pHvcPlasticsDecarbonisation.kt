package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the decarbonisation of the HVC Plastics sector
*/
data class P2pHvcPlasticsDecarbonisation(
    val energyMix: BigDecimal? = null,

    val electrification: BigDecimal? = null,
)
