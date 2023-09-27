package org.dataland.datalandbackend.model.p2p.categories.residentialRealEstate.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy source" belonging to the category "Residential Real Estate" of the p2p framework.
*/
data class P2pResidentialRealEstateEnergySource(
    val renewableHeatingInPercent: BigDecimal? = null,
)
