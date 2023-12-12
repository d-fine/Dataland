package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.eutaxonomy.ConstantParameters
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    val totalAmount: CurrencyDataPoint?,
    @field:Valid
    val nonEligibleShare: RelativeAndAbsoluteFinancialShare?,
    @field:Valid
    val eligibleShare: RelativeAndAbsoluteFinancialShare?,
    @field:Valid
    val nonAlignedShare: RelativeAndAbsoluteFinancialShare?,
    @field:Valid
    val nonAlignedActivities: List<EuTaxonomyActivity>?,
    @field:Valid
    val alignedShare: RelativeAndAbsoluteFinancialShare?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val substantialContributionToClimateChangeMitigationInPercent: BigDecimal?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val substantialContributionToClimateChangeAdaptionInPercent: BigDecimal?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: BigDecimal?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val substantialContributionToTransitionToACircularEconomyInPercent: BigDecimal?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val substantialContributionToPollutionPreventionAndControlInPercent: BigDecimal?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: BigDecimal?,
    @field:Valid
    val alignedActivities: List<EuTaxonomyAlignedActivity>?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val enablingShareInPercent: BigDecimal?,
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val transitionalShareInPercent: BigDecimal?,
)
