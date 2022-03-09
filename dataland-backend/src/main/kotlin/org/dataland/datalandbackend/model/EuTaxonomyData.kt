package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class EuTaxonomyData(
    @field:JsonProperty("Amount €") val amount: java.math.BigDecimal? = null,
    @field:JsonProperty("Taxonomy-aligned proportion of turnover %") val proportion: java.math.BigDecimal? = null
)
