package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Figures to be filled out for some of the EU-Taxonomy questionnaire fields
 */
data class EuTaxonomyData(
    @field:JsonProperty("total") val total: java.math.BigDecimal? = null,
    @field:JsonProperty("aligned_turnover") val aligned: java.math.BigDecimal? = null,
    @field:JsonProperty("eligible_turnover") val eligible: java.math.BigDecimal? = null
)
