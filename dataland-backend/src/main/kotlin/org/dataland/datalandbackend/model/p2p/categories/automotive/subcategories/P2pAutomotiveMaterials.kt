package org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Materials" belonging to the category "Automotive" of the p2p framework.
*/
data class P2pAutomotiveMaterials(
    val materialUseManagementInPercent: BigDecimal? = null,
    val useOfSecondaryMaterialsInPercent: BigDecimal? = null,
)
