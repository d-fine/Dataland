package org.dataland.datalandbackend.model.sme.categories.production.subcategories

import org.dataland.datalandbackend.model.generics.Address
import org.dataland.datalandbackend.model.generics.ProductionSiteBase
import java.math.BigDecimal

/**
 * Production Sites for Sme framework
 */
data class SmeProductionSite(
    override val nameOfProductionSite: String?,
    override val addressOfProductionSite: Address,
    val shareOfTotalRevenueInPercent: BigDecimal?,
) : ProductionSiteBase(nameOfProductionSite, addressOfProductionSite)
