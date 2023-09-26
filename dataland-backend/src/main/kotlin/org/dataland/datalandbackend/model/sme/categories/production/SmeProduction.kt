package org.dataland.datalandbackend.model.sme.categories.production

import org.dataland.datalandbackend.model.sme.categories.production.subcategories.SmeProductionProducts
import org.dataland.datalandbackend.model.sme.categories.production.subcategories.SmeProductionSites

/**
 * --- API model ---
 * Fields of the category "Production" of the sme framework.
*/
data class SmeProduction(
    val sites: SmeProductionSites? = null,

    val products: SmeProductionProducts? = null,
)
