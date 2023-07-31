package org.dataland.datalandbackend.model.sme.categories.general.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Company Financials" belonging to the category "General" of the sme framework.
 */
data class SmeGeneralCompanyFinancials(
    val revenueInEur: BigDecimal? = null,

    val operatingCostInEur: BigDecimal? = null,

    val capitalAssetsInEur: BigDecimal? = null,
)
