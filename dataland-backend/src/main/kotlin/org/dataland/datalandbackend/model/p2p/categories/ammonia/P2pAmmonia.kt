package org.dataland.datalandbackend.model.p2p.categories.ammonia

import org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories.P2pAmmoniaDecarbonisation
import org.dataland.datalandbackend.model.p2p.categories.ammonia.subcategories.P2pAmmoniaDefossilisation

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the ammonia sector
 */
data class P2pAmmonia(
    val decarbonisation: P2pAmmoniaDecarbonisation? = null,

    val defossilisation: P2pAmmoniaDefossilisation? = null,
)
