package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPointOneValue
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    val totalAmount: DataPointOneValue<AmountWithCurrency>?,
    val nonEligibleShare: RelativeAndAbsoluteFinancialShare?,
    val eligibleShare: RelativeAndAbsoluteFinancialShare?,
    val nonAlignedShare: RelativeAndAbsoluteFinancialShare?,
    val nonAlignedActivities: List<EuTaxonomyActivity>?,
    val alignedShare: RelativeAndAbsoluteFinancialShare?,
    val substantialContributionToClimateChangeMitigation: BigDecimal?,
    val substantialContributionToClimateChangeAdaption: BigDecimal?,
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: BigDecimal?,
    val substantialContributionToTransitionToACircularEconomy: BigDecimal?,
    val substantialContributionToPollutionPreventionAndControl: BigDecimal?,
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: BigDecimal?,
    val alignedActivities: List<EuTaxonomyAlignedActivity>?,
    val enablingShare: BigDecimal?,
    val transitionalShare: BigDecimal?,
)
