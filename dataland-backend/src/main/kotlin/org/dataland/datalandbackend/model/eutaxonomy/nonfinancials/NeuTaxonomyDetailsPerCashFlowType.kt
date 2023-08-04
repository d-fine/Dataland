package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import java.math.BigDecimal

data class NeuTaxonomyDetailsPerCashFlowType(
    val alignedActivities: List<NeuTaxonomyAlignedActivity>,
    val alignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
    val eligibleNotAlignedActivities: List<NeuTaxonomyActivity>,
    val eligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO is this confusing with eligibleNotAlignedShare?
//    val nonEligibleShare: DataPointAbsoluteAndPercentage<BigDecimal>, // TODO this seems redundant
    val totalShare: DataPointAbsoluteAndPercentage<BigDecimal>,
//    val enablingAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
//    val transitionalAlignedShare: DataPointAbsoluteAndPercentage<BigDecimal>,
)
