package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    val totalAmount: DataPoint<BigDecimal>,
    val totalNonEligibleShare: FinancialShare,
    val totalEligibleShare: FinancialShare,
    val totalEligibleNonAlignedShare: FinancialShare,
    val eligibleNotAlignedActivities: List<EuTaxonomyActivity>,
    val totalAlignedShare: FinancialShare,
    val alignedActivities: List<EuTaxonomyAlignedActivity>,
    val enablingAlignedShare: BigDecimal,
    val transitionalAlignedShare: BigDecimal,
)
