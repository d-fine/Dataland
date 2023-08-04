package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

data class NeuTaxonomyDataForNonFinancials(
    val generalThings: String,

    val revenue: NeuTaxonomyDetailsPerCashFlowType, // TODO should we rename "revenue" to "turnover"?
    val capex: NeuTaxonomyDetailsPerCashFlowType,
    val opex: NeuTaxonomyDetailsPerCashFlowType,
)// : EuTaxonomyCommonFields, FrameworkBase
