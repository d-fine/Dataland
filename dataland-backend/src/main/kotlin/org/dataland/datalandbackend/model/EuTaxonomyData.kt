package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class EuTaxonomyData (
    @field:JsonProperty("FieldName") val fieldName: java.math.BigDecimal? = null,
    @field:JsonProperty("OtherFieldName") val otherFieldName: java.math.BigDecimal? = null
)