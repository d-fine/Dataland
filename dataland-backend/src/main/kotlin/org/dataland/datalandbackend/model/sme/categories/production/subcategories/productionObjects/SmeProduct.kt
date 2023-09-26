package org.dataland.datalandbackend.model.sme.categories.production.subcategories.productionObjects

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.generics.ProductBase
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SME questionnaire regarding a single product for the "Most Important Products" field
 */
data class SmeProduct(
    @field:JsonProperty(required = true)
    override val name: String,

    val shareOfTotalRevenueInPercent: BigDecimal?,
) : ProductBase(name)
