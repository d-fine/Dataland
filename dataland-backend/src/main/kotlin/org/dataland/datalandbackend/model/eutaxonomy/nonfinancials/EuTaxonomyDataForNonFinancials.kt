package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */

data class EuTaxonomyDataForNonFinancials(
    val general: EuTaxonomyGeneral?,

    val revenue: EuTaxonomyDetailsPerCashFlowType?,
    val capex: EuTaxonomyDetailsPerCashFlowType?,
    val opex: EuTaxonomyDetailsPerCashFlowType?,
)
