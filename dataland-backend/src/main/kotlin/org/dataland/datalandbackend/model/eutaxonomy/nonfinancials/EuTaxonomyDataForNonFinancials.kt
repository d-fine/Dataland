package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@DataType("eutaxonomy-non-financials")
data class EuTaxonomyDataForNonFinancials(
    @field:JsonProperty("capex")
    val capex: EuTaxonomyDetailsPerCashFlowType? = null,

    @field:JsonProperty("opex")
    val opex: EuTaxonomyDetailsPerCashFlowType? = null,

    @field:JsonProperty("revenue")
    val revenue: EuTaxonomyDetailsPerCashFlowType? = null,

    @field:JsonProperty("attestation", required = true)
    val attestation: AssuranceData? = null,

    @field:JsonProperty("reportingObligation", required = true)
    val reportObligation: YesNo? = null,

    @field:JsonProperty("activityLevelReporting", required = true)
    val activityLevelReporting: YesNo? = null
)
