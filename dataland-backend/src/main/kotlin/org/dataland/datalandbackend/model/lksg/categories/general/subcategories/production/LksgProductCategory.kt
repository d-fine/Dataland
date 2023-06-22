package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a single product category for the "Product Categories" field
 */
data class LksgProductCategory(
    val definitionProductTypeService: List<String>?, // TODO proposal: non empty list required
    val suppliersPerCountry: List<LksgCountryAssociatedSuppliers>?,
    val orderVolume: BigDecimal?, // TODO should this be required? It is about important products and this field weights the importance
)
