package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import java.math.BigDecimal

data class NeuTaxonomyDetailsPerCashFlowType(
    val totalShare: DataPoint<BigDecimal>,
    val totalNonEligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO this seems redundant
    val totalEligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO is this confusing with eligibleNotAlignedShare?
    val totalEligibleNonAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO is this confusing with eligibleNotAlignedShare?
    val eligibleNotAlignedActivities: List<NeuTaxonomyActivity>, // TODO FE convert this list to an object properties of the parent
    val totalAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
    val alignedActivities: List<NeuTaxonomyAlignedActivity>, // TODO FE convert this list to an object properties of the parent
    val enablingAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
    val transitionalAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
)
