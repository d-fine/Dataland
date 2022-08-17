package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.eutaxonomy.AttestationOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo

/**
 * --- API model ---
 * Fields of the questionnaire for EU-Taxonomy data for non-financial companies
 */
@DataType
data class EuTaxonomyDataForNonFinancials(
    @field:JsonProperty("capex") val capex: EuTaxonomyDetailsPerCashFlowType? = null,
    @field:JsonProperty("opex") val opex: EuTaxonomyDetailsPerCashFlowType? = null,
    @field:JsonProperty("revenue") val revenue: EuTaxonomyDetailsPerCashFlowType? = null,
    @field:JsonProperty("reportingObligation", required = true) val reportObligation: YesNo? = null,
    @field:JsonProperty("attestation", required = true) val attestation: AttestationOptions? = null
)
