package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a single product for the "Most Important Products" field
 */
data class LksgProduct(
    @field:JsonProperty(required = true)
    val productName: String,

    val productionSteps: List<String>?,

    val relatedCorporateSupplyChain: String?,
)
