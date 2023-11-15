package org.dataland.datalandbackend.model.sme.categories.general.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Company Financials" belonging to the category "General" of the sme framework.
*/
data class SmeGeneralCompanyFinancials(
    val revenueInEUR: BigDecimal? = null,

    val operatingCostInEUR: BigDecimal? = null,

    val capitalAssetsInEUR: BigDecimal? = null,
)
