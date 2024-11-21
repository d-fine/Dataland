package org.dataland.datalandbackend.model.p2p.categories.automotive

import org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories.P2pAutomotiveEnergy
import org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories.P2pAutomotiveMaterials
import org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories.P2pAutomotiveTechnologyValueCreation

/**
 * --- API model ---
 * Fields of the category "Automotive" of the p2p framework.
*/
data class P2pAutomotive(
    val energy: P2pAutomotiveEnergy? = null,
    val technologyValueCreation: P2pAutomotiveTechnologyValueCreation? = null,
    val materials: P2pAutomotiveMaterials? = null,
)
