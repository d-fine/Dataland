package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

// TODO for which of these fields is actual evidence required?
open class NeuTaxonomyActivity(
    val activityId: String, // TODO use an enum instead
    val naceCodes: List<String>,
    val share: DataPointAbsoluteAndPercentage<BigDecimal>?,
)

// TODO for which of these fields is actual evidence required?
class NeuTaxonomyAlignedActivity(
    activityId: String, // TODO use an enum instead
    naceCodes: List<String>,
    share: DataPointAbsoluteAndPercentage<BigDecimal>?,
    val substantialContributionCriteria: Map<NeuTaxonomyCriterion, DataPoint<BigDecimal>>?,
    val dnshCriteria: Map<NeuTaxonomyCriterion, YesNo>?,
    val minimumSafeguards: YesNo?,
//    val alignedShareForThisYear: DataPoint<BigDecimal>?, // TODO redundant to field "share"?
//    val alignedShareForLastYear: DataPoint<BigDecimal>?, // TODO neccessary?
    // TODO enabling or transitional flags must not be provided
    //  because they can be obtained from the provided json
) : NeuTaxonomyActivity(activityId, naceCodes, share)