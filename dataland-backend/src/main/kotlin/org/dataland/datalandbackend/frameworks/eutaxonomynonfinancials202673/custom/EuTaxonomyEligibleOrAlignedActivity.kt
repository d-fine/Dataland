package org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.RelativeAndAbsoluteFinancialShare
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import java.math.BigDecimal

/**
 * --- API model ---
 * This class represents an activity related to the EU taxonomy framework
 * with fields regarding the fulfillment of criteria regarding the alignment to EU taxonomy regulation
 */
data class EuTaxonomyEligibleOrAlignedActivity(
    @JsonProperty(required = true)
    val activityName: Activity,
    val naceCodes: List<String>?,
    val share: RelativeAndAbsoluteFinancialShare?,
    val relativeEligibleShareInPercent: BigDecimal?,
    val substantialContributionToClimateChangeMitigationInPercent: BigDecimal?,
    val substantialContributionToClimateChangeAdaptationInPercent: BigDecimal?,
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: BigDecimal?,
    val substantialContributionToTransitionToACircularEconomyInPercent: BigDecimal?,
    val substantialContributionToPollutionPreventionAndControlInPercent: BigDecimal?,
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: BigDecimal?,
    val enablingActivity: YesNo?,
    val transitionalActivity: YesNo?,
)
