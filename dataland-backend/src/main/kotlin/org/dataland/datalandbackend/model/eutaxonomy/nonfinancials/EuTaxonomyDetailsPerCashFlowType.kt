package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
        val totalShare: DataPoint<BigDecimal>,
        val totalNonEligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>,
        val totalEligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>,
        val totalEligibleNonAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
        val eligibleNotAlignedActivities: List<EuTaxonomyActivity>,
        val totalAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
        val alignedActivities: List<EuTaxonomyAlignedActivity>,
        val enablingAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
        val transitionalAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
)
