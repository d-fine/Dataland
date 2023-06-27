package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding climate targets
 */
data class P2pClimateTargets(
    val longTermScienceBasedClimateTarget: YesNo?,

    val shortTermScienceBasedClimateTarget: YesNo?,
)
