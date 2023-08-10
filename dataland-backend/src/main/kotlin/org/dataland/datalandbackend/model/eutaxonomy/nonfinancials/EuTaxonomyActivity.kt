package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

open class EuTaxonomyActivity(
    val activityName: String, // TODO use an enum instead
    val naceCodes: List<String>,
    val share: DataPointAbsoluteAndPercentage<BigDecimal>?,
)

class EuTaxonomyAlignedActivity(
        activityName: String, // TODO use an enum instead
        naceCodes: List<String>,
        share: DataPointAbsoluteAndPercentage<BigDecimal>?,
        val substantialContributionCriteria: Map<EuTaxonomyCriterion, DataPoint<BigDecimal>>?,
        val dnshCriteria: Map<EuTaxonomyCriterion, YesNo>?,
        val minimumSafeguards: YesNo?,
) : EuTaxonomyActivity(activityName, naceCodes, share)