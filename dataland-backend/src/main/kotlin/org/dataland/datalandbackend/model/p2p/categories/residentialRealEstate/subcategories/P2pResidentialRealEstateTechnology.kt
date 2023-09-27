package org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Technology" belonging to the category "Residential Real Estate" of the p2p framework.
*/
data class P2pResidentialRealEstateTechnology(
    val useOfDistrictHeatingNetworksInPercent: BigDecimal? = null,

    val heatPumpUsageInPercent: BigDecimal? = null,
)
