package org.dataland.datalandbackend.model.p2p.categories.steel

import org.dataland.datalandbackend.model.p2p.categories.steel.subcategories.P2pSteelEnergy
import org.dataland.datalandbackend.model.p2p.categories.steel.subcategories.P2pSteelTechnology

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the steel sector
*/
data class P2pSteel(
    val energy: P2pSteelEnergy? = null,

    val technology: P2pSteelTechnology? = null,
)
