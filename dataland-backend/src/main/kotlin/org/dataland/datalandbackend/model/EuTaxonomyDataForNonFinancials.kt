package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.AttestationOptions
import org.dataland.datalandbackend.model.enums.YesNo

/**
 * --- API model ---
 * Fields of the questionnaire for EU-Taxonomy data for non-financial companies
 */
@DataType
data class EuTaxonomyDataForNonFinancials(
    @field:JsonProperty("Capex") val capex: EuTaxonomyDetailsPerCashFlowType? = null,
    @field:JsonProperty("Opex") val opex: EuTaxonomyDetailsPerCashFlowType? = null,
    @field:JsonProperty("Revenue") val revenue: EuTaxonomyDetailsPerCashFlowType? = null,
    @field:JsonProperty("Reporting Obligation", required = true) val reportObligation: YesNo? = null,
    @field:JsonProperty("Attestation", required = true) val attestation: AttestationOptions? = null
)
