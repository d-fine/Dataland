package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Figures to be filled out for some of the EU-Taxonomy questionnaire fields
 */
data class EuTaxonomyDetailsPerCashFlowType(
    @field:JsonProperty("totalAmount") val totalAmount: BigDecimal? = null,
    @field:JsonProperty("alignedPercentage") val alignedPercentage: BigDecimal? = null,
    @field:JsonProperty("eligiblePercentage") val eligiblePercentage: BigDecimal? = null
)
