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
    // TODO these names differ from the ones specified in the data dictionary due to length.
    //  However, their purpose is clear from the names here.
    val substantialContributionToClimateChangeMitigation: BigDecimal?,
    val substantialContributionToClimateChangeAdaption: BigDecimal?,
    val substantialContributionToSustainableWaterUse: BigDecimal?,
    val substantialContributionToCircularEconomy: BigDecimal?,
    val substantialContributionToPollutionPreventionAndControl: BigDecimal?,
    val substantialContributionToBiodiversity: BigDecimal?,
    // TODO these names differ from the ones specified in the data dictionary due to length.
    //  However, their purpose is clear from the names here.
    val dnshToClimateChangeMitigation: YesNo?,
    val dnshToClimateChangeAdaption: YesNo?,
    val dnshToSustainableWaterUse: YesNo?,
    val dnshToCircularEconomy: YesNo?,
    val dnshToPollutionPreventionAndControl: YesNo?,
    val dnshToBiodiversity: YesNo?,
    val minimumSafeguards: YesNo?,
)
