package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Climate Targets" belonging to the category "General" of the p2p framework.
*/
data class P2pGeneralClimateTargets(
    val shortTermScienceBasedClimateTarget: YesNo? = null,
    val longTermScienceBasedClimateTarget: YesNo? = null,
)
