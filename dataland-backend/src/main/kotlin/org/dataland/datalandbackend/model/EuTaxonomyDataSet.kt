package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class EuTaxonomyDataSet(
    @field:JsonProperty("Capex") val capex: EuTaxonomyData? = null,
    @field:JsonProperty("Opex") val opex: EuTaxonomyData? = null,
    @field:JsonProperty("Revenues") val revenues: EuTaxonomyData? = null,
    @field:JsonProperty("Reporting Obligation", required = true) val reportObligation: YesNo? = null,
    @field:JsonProperty("Attestation", required = true) val attestation: AttestationOptions? = null
) {
    enum class YesNo { Yes, No }
    enum class AttestationOptions { None, Some, Full }
}
