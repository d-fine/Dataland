package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.EnvironmentalObjective
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
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
    @field:Schema(example = JsonExampleFormattingConstants.SUBSTANTIAL_CONTRIBUTION_CRITIREA)
    val substantialContributionCriteria: Map<EnvironmentalObjective, BigDecimal>?,
    @field:Schema(example = JsonExampleFormattingConstants.DNSH_CRITIREA)
    val dnshCriteria: Map<EnvironmentalObjective, YesNo>?,
    val minimumSafeguards: YesNo?,
)
