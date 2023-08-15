package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.EnvironmentalObjective
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class NewEuTaxonomyDetailsPerCashFlowType(
        val totalAmount: DataPointOneValue<BigDecimal>?,
        val totalNonEligibleShare: FinancialShare?,
        val totalEligibleShare: FinancialShare?,
        val totalNonAlignedShare: FinancialShare?,
        val nonAlignedActivities: List<EuTaxonomyActivity>?,
        val totalAlignedShare: FinancialShare?,
        val substantialContributionCriteria: Map<EnvironmentalObjective, BigDecimal>?,
        val alignedActivities: List<EuTaxonomyAlignedActivity>?,
)
