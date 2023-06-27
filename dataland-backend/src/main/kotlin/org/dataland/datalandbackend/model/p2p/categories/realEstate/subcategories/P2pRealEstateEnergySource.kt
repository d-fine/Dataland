package org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the building energy source of the real estate sector
 */
data class P2pRealEstateEnergySource(
    val renewableHeating: BigDecimal?,
)
