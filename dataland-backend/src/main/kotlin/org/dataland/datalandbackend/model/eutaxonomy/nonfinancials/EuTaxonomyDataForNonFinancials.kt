package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@DataType("eutaxonomy-non-financials")
data class EuTaxonomyDataForNonFinancials(
    @JsonProperty(required = true)
    val generalThings: EuTaxonomyGeneral,

    val revenue: EuTaxonomyDetailsPerCashFlowType?, // TODO should we rename "revenue" to "turnover"?
    val capex: EuTaxonomyDetailsPerCashFlowType?,
    val opex: EuTaxonomyDetailsPerCashFlowType?,
)
