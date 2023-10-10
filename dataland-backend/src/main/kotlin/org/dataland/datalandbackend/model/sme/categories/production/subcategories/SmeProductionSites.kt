package org.dataland.datalandbackend.model.sme.categories.production.subcategories

import org.dataland.datalandbackend.model.sme.categories.production.subcategories.productionObjects.SmeProductionSite

/**
 * --- API model ---
 * Fields of the subcategory "Sites" belonging to the category "Production" of the sme framework.
*/
data class SmeProductionSites(
    val listOfProductionSites: List<SmeProductionSite>? = null,
)
