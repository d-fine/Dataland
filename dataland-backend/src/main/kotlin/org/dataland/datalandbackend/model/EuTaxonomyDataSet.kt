package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Fields of the questionnaire for EU-Taxonomy data
 */
data class EuTaxonomyDataSet(
    @field:JsonProperty("Capex") val capex: EuTaxonomyData? = null,
    @field:JsonProperty("Opex") val opex: EuTaxonomyData? = null,
    @field:JsonProperty("Revenue") val revenue: EuTaxonomyData? = null,
    @field:JsonProperty("Reporting Obligation", required = true) val reportObligation: YesNo? = null,
    @field:JsonProperty("Attestation", required = true) val attestation: AttestationOptions? = null
) {
    /**
     * Possible options to specify if obligation to report exists
     */
    enum class YesNo { Yes, No }

    /**
     * Possible options to specify how the report figures were attested
     */
    enum class AttestationOptions { None, Some, Full }
}
