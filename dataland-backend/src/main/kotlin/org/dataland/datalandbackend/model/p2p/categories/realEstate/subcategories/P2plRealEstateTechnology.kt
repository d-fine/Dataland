package org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the used technology in the real estate sector
 */
data class P2plRealEstateTechnology(
    val useOfDistrictHeatingNetworksInPercent: BigDecimal? = null,
    val heatPumpUsageInPercent: BigDecimal? = null,
)
