package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a product step of a product
 */
data class LksgProductionStep(
    val productionStepName: String?, // TODO this should be required, I guess. Data dictionary says optional
    val relatedCorporateSupplyChain: List<String>?,
)
