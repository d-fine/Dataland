package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

// TODO for which of these fields is actual evidence required?
open class NeuTaxonomyActivity(
    val activityName: String, // TODO use an enum instead
    val naceCodes: List<String>,
    val share: DataPointAbsoluteAndPercentage<BigDecimal>?,
)

// TODO for which of these fields is actual evidence required?
class NeuTaxonomyAlignedActivity(
    activityName: String, // TODO use an enum instead
    naceCodes: List<String>,
    share: DataPointAbsoluteAndPercentage<BigDecimal>?,
    val substantialContributionCriteria: Map<NeuTaxonomyCriterion, DataPoint<BigDecimal>>?,
    val dnshCriteria: Map<NeuTaxonomyCriterion, YesNo>?,
    val minimumSafeguards: YesNo?,
) : NeuTaxonomyActivity(activityName, naceCodes, share)