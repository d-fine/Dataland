package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPointOneValue
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    val totalAmount: DataPointOneValue<AmountWithCurrency>?,
    val totalNonEligibleShare: RelativeAndAbsoluteFinancialShare?,
    val totalEligibleShare: RelativeAndAbsoluteFinancialShare?,
    val totalNonAlignedShare: RelativeAndAbsoluteFinancialShare?,
    val nonAlignedActivities: List<EuTaxonomyActivity>?,
    val totalAlignedShare: RelativeAndAbsoluteFinancialShare?,
    // TODO these names differ from the ones specified in the data dictionary due to length.
    //  However, their purpose is clear from the names here.
    val substantialContributionToClimateChangeMitigation: BigDecimal?,
    val substantialContributionToClimateChangeAdaption: BigDecimal?,
    val substantialContributionToSustainableWaterUse: BigDecimal?,
    val substantialContributionToCircularEconomy: BigDecimal?,
    val substantialContributionToPollutionPreventionAndControl: BigDecimal?,
    val substantialContributionToBiodiversity: BigDecimal?,
    val alignedActivities: List<EuTaxonomyAlignedActivity>?,
    val totalEnablingShare: BigDecimal?,
    val totalTransitionalShare: BigDecimal?,
)
