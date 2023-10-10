package org.dataland.datalandbackend.model.sme.categories.production.subcategories

import org.dataland.datalandbackend.model.sme.categories.production.subcategories.productionObjects.SmeProduct

/**
 * --- API model ---
 * Fields of the subcategory "Products" belonging to the category "Production" of the sme framework.
*/
data class SmeProductionProducts(
    val listOfProducts: List<SmeProduct>? = null,
)
