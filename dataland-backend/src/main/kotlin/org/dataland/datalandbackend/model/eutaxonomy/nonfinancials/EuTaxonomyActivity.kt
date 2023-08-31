package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
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
    val substantialContributionToClimateChangeMitigation: BigDecimal?,
    val substantialContributionToClimateChangeAdaption: BigDecimal?,
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: BigDecimal?,
    val substantialContributionToTransitionToACircularEconomy: BigDecimal?,
    val substantialContributionToPollutionPreventionAndControl: BigDecimal?,
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: BigDecimal?,
    val dnshToClimateChangeMitigation: YesNo?,
    val dnshToClimateChangeAdaption: YesNo?,
    val dnshToSustainableUseAndProtectionOfWaterAndMarineResources: YesNo?,
    val dnshToTransitionToACircularEconomy: YesNo?,
    val dnshToPollutionPreventionAndControl: YesNo?,
    val dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: YesNo?,
    val minimumSafeguards: YesNo?,
)
