package org.dataland.datalandbackend.model.p2p.categories.steel

import org.dataland.datalandbackend.model.p2p.categories.steel.subcategories.P2pSteelEnergy
import org.dataland.datalandbackend.model.p2p.categories.steel.subcategories.P2pSteelTechnology

/**
 * --- API model ---
 * Fields of the category "Steel" of the p2p framework.
*/
data class P2pSteel(
    val energy: P2pSteelEnergy? = null,
    val technology: P2pSteelTechnology? = null,
)
