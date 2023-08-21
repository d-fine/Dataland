package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@DataType("new-eutaxonomy-non-financials")
data class NewEuTaxonomyDataForNonFinancials(
    @JsonProperty(required = true)
    val general: EuTaxonomyGeneral,

    val revenue: NewEuTaxonomyDetailsPerCashFlowType?,
    val capex: NewEuTaxonomyDetailsPerCashFlowType?,
    val opex: NewEuTaxonomyDetailsPerCashFlowType?,
)
