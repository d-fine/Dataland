package org.dataland.datalandbackend.frameworks.lksg.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.frameworks.ProductBase

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a single product for the "Most Important Products" field
 */
data class LksgProduct(
    @field:JsonProperty(required = true)
    override val name: String,
    val productionSteps: List<String>?,
    val relatedCorporateSupplyChain: String?,
) : ProductBase
