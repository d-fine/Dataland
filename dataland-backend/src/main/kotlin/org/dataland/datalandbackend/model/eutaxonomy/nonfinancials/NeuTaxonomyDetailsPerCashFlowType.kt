package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import java.math.BigDecimal

data class NeuTaxonomyDetailsPerCashFlowType(
    val alignedActivities: List<NeuTaxonomyAlignedActivity>, // TODO FE convert this list to an object properties of the parent
    val alignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
    val eligibleNotAlignedActivities: List<NeuTaxonomyActivity>, // TODO FE convert this list to an object properties of the parent
    val eligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO is this confusing with eligibleNotAlignedShare?
//    val nonEligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO this seems redundant
    val totalShare: DataPointAbsoluteAndPercentage<BigDecimal>,
//    val enablingAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
//    val transitionalAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
)
