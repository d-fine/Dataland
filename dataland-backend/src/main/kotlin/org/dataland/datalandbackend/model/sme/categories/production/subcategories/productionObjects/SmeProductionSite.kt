package org.dataland.datalandbackend.model.sme.categories.production.subcategories.productionObjects

import org.dataland.datalandbackend.interfaces.frameworks.ProductionSite
import org.dataland.datalandbackend.model.generics.Address
import java.math.BigDecimal

/**
 * Production Sites for Sme framework
 */
data class SmeProductionSite(
    override val nameOfProductionSite: String?,
    override val addressOfProductionSite: Address,
    val shareOfTotalRevenueInPercent: BigDecimal?,
) : ProductionSite
