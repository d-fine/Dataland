package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.eutaxonomy.ConstantParameters
import java.math.BigDecimal

/**
 * --- API model ---
 * This class represents an activity related to the EU taxonomy framework
 */
data class EuTaxonomyActivity(
    @JsonProperty(required = true)
    val activityName: Activity,
    val naceCodes: List<String>?,
    val share: RelativeAndAbsoluteFinancialShare?,
)

/**
 * --- API model ---
 * This class represents an activity related to the EU taxonomy framework
 * with fields regarding the fulfillment of criteria regarding the alignment to EU taxonomy regulation
 */

data class EuTaxonomyAlignedActivity(
    @JsonProperty(required = true)
    val activityName: Activity,
    val naceCodes: List<String>?,
    val share: RelativeAndAbsoluteFinancialShare?,
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
    @Min(ConstantParameters.PERCENT_MINIMUM)
    @Max(ConstantParameters.PERCENT_MAXIMUM)
    val dnshToClimateChangeMitigation: YesNo?,
    val dnshToClimateChangeAdaption: YesNo?,
    val dnshToSustainableUseAndProtectionOfWaterAndMarineResources: YesNo?,
    val dnshToTransitionToACircularEconomy: YesNo?,
    val dnshToPollutionPreventionAndControl: YesNo?,
    val dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: YesNo?,
    val minimumSafeguards: YesNo?,
)
