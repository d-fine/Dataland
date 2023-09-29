package org.dataland.datalandbackend.model.sme.categories.production.subcategories.productionObjects

import org.dataland.datalandbackend.model.generics.Address
import org.dataland.datalandbackend.interfaces.ProductionSiteInterface
import java.math.BigDecimal

/**
 * Production Sites for Sme framework
 */
data class SmeProductionSite(
    override val nameOfProductionSite: String?,
    override val addressOfProductionSite: Address,
    val shareOfTotalRevenueInPercent: BigDecimal?,
) : ProductionSiteInterface
