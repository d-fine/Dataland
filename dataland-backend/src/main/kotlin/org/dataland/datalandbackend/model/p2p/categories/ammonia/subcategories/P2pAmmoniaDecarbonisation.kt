package org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the decarbonization of ammonia production
*/
data class P2pAmmoniaDecarbonisation(
    val energyMixInPercent: BigDecimal? = null,

    val ccsTechnologyAdoptionInPercent: BigDecimal? = null,

    val electrificationInPercent: BigDecimal? = null,
)
