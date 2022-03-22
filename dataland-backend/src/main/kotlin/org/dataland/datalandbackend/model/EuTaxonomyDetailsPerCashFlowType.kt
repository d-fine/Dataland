package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Figures to be filled out for some of the EU-Taxonomy questionnaire fields
 */
data class EuTaxonomyDetailsPerCashFlowType(
    @field:JsonProperty("total") val total: java.math.BigDecimal? = null,
    @field:JsonProperty("aligned") val aligned: java.math.BigDecimal? = null,
    @field:JsonProperty("eligible") val eligible: java.math.BigDecimal? = null
)
