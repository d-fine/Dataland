package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.EnvironmentalObjective
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class NewEuTaxonomyDetailsPerCashFlowType(
    val totalAmount: DataPointOneValue<AmountWithCurrency>?,
    val totalNonEligibleShare: RelativeAndAbsoluteFinancialShare?,
    val totalEligibleShare: RelativeAndAbsoluteFinancialShare?,
    val totalNonAlignedShare: RelativeAndAbsoluteFinancialShare?,
    val nonAlignedActivities: List<EuTaxonomyActivity>?,
    val totalAlignedShare: RelativeAndAbsoluteFinancialShare?,
    @field:Schema(example = JsonExampleFormattingConstants.SUBSTANTIAL_CONTRIBUTION_CRITIREA)
    val substantialContributionCriteria: Map<EnvironmentalObjective, BigDecimal>?,
    val alignedActivities: List<EuTaxonomyAlignedActivity>?,
    val totalEnablingShare: BigDecimal?,
    val totalTransitionalShare: BigDecimal?,
)
