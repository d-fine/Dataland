package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.CurrencyDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    val totalAmount: CurrencyDataPoint<BigDecimal>?,
    val nonEligibleShare: RelativeAndAbsoluteFinancialShare?,
    val eligibleShare: RelativeAndAbsoluteFinancialShare?,
    val nonAlignedShare: RelativeAndAbsoluteFinancialShare?,
    val nonAlignedActivities: List<EuTaxonomyActivity>?,
    val alignedShare: RelativeAndAbsoluteFinancialShare?,
    val substantialContributionToClimateChangeMitigationInPercent: BigDecimal?,
    val substantialContributionToClimateChangeAdaptionInPercent: BigDecimal?,
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: BigDecimal?,
    val substantialContributionToTransitionToACircularEconomyInPercent: BigDecimal?,
    val substantialContributionToPollutionPreventionAndControlInPercent: BigDecimal?,
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: BigDecimal?,
    val alignedActivities: List<EuTaxonomyAlignedActivity>?,
    val enablingShareInPercent: BigDecimal?,
    val transitionalShareInPercent: BigDecimal?,
)
