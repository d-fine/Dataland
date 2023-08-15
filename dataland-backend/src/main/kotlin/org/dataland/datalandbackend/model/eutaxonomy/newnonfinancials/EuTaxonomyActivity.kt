package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.EnvironmentalObjective
import java.math.BigDecimal

/**
 * --- API model ---
 * This class represents an activity related to the EU taxonomy famework
 */
open class EuTaxonomyActivity(
    @JsonProperty(required = true)
    val activityName: Activity,
    val naceCodes: List<String>?,
    val share: FinancialShare?,
)

/**
 * --- API model ---
 * This class represents an activity related to the EU taxonomy famework
 * with fields regarding the fulfillment of criteria regarding the alignment to EU taxonomy regulation
 */
class EuTaxonomyAlignedActivity(
    @JsonProperty(required = true)
    activityName: Activity,
    naceCodes: List<String>?,
    share: FinancialShare?,
    val substantialContributionCriteria: Map<EnvironmentalObjective, BigDecimal>?,
    val dnshCriteria: Map<EnvironmentalObjective, YesNo>?,
    val minimumSafeguards: YesNo?,
) : EuTaxonomyActivity(activityName, naceCodes, share)
