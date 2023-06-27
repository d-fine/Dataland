package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding investment planning.
 */
data class P2pInvestmentPlanning(
    val capexShareInGhgIntensivePlants: BigDecimal? = null,

    val capexShareInNetZeroSolutions: BigDecimal? = null,

    val investmentPlanForClimateTargets: YesNo? = null,

    // Note: This field is called randD instead of rAndD as swagger really does not like you using rAndD
    val randDExpenditureForNetZeroSolutions: BigDecimal? = null,
)
