package org.dataland.datalandbackend.model.sme.categories.general.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Business Numbers" belonging to the category "General" of the sme framework.
*/
data class SmeGeneralBusinessNumbers(
    val revenueInEur: BigDecimal? = null,

    val operatingCostInEur: BigDecimal? = null,

    val capitalAssetsInEur: BigDecimal? = null,
)
