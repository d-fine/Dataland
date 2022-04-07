package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Figures to be filled out for some of the EU-Taxonomy questionnaire fields
 */
data class EuTaxonomyDetailsPerCashFlowType(
    @field:JsonProperty("total") val total: BigDecimal? = null,
    @field:JsonProperty("aligned") val aligned: BigDecimal? = null,
    @field:JsonProperty("eligible") val eligible: BigDecimal? = null
)
