package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.annotations.DataType

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@Suppress("MagicNumber")
@DataType("eutaxonomy-non-financials", 2)
data class EuTaxonomyDataForNonFinancials(
    val general: EuTaxonomyGeneral?,

    val revenue: EuTaxonomyDetailsPerCashFlowType?,
    val capex: EuTaxonomyDetailsPerCashFlowType?,
    val opex: EuTaxonomyDetailsPerCashFlowType?,
)
