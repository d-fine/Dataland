package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding investment planning.
 */
data class P2pInvestmentPlanning(
    val capexShareInGhgIntensivePlants: BigDecimal? = null, // TODO changes hopefully

    val capexShareInNetZeroSolutions: BigDecimal? = null, // TODO changes hopefully

    val investmentPlanForClimateTargets: YesNo? = null,

    val researchAndDevelopmentExpenditureForNetZeroSolutions: BigDecimal? = null, // TODO changes hopefully
)
