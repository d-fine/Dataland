package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class EuTaxonomyDataSet (
    @field:JsonProperty("capex") val capex: EuTaxonomyData? = null,
    @field:JsonProperty("opex") val opex: EuTaxonomyData? = null,
    @field:JsonProperty("revenues") val revenues: EuTaxonomyData? = null,
    @field:JsonProperty("reportingObligation") val reportObligation: String? = null,
    @field:JsonProperty("attestation") val attestation: String? = null
)