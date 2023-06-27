package org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the decarbonization of ammonia production
*/
data class P2pAmmoniaDecarbonisation(
    val energyMix: BigDecimal? = null,

    val ccsTechnologyAdoption: BigDecimal? = null,

    val electrification: BigDecimal? = null,
)
