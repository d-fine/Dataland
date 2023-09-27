package org.dataland.datalandbackend.model.p2p.categories.commercialRealEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Technology" belonging to the category "Commercial Real Estate" of the p2p framework.
*/
data class P2pCommercialRealEstateTechnology(
    val useOfDistrictHeatingNetworksInPercent: BigDecimal? = null,

    val heatPumpUsageInPercent: BigDecimal? = null,
)
