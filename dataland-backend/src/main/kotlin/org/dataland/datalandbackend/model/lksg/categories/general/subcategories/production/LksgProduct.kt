package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a single product for the "Most Important Products" field
 */
data class LksgProduct(
    val productName: String?, // TODO this should be required, I guess. Data dictionary says optional
    val productionSteps: List<String>?,
    val relatedCorporateSupplyChain: List<String>?,
)
