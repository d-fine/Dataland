package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import jakarta.validation.Valid
import org.dataland.datalandbackend.annotations.DataType

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@DataType("eutaxonomy-non-financials")
data class EuTaxonomyDataForNonFinancials(
    @field:Valid
    val general: EuTaxonomyGeneral?,

    @field:Valid
    val revenue: EuTaxonomyDetailsPerCashFlowType?,
    @field:Valid
    val capex: EuTaxonomyDetailsPerCashFlowType?,
    @field:Valid
    val opex: EuTaxonomyDetailsPerCashFlowType?,
)
