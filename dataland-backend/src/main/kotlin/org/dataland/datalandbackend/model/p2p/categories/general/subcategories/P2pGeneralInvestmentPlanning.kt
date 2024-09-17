package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model --- Fields of the subcategory "Investment planning" belonging to the category
 * "General" of the p2p framework.
 */
data class P2pGeneralInvestmentPlanning(
  val investmentPlanForClimateTargets: YesNo? = null,
  val capexShareInNetZeroSolutionsInPercent: BigDecimal? = null,
  val capexShareInGhgIntensivePlantsInPercent: BigDecimal? = null,
  val researchAndDevelopmentExpenditureForNetZeroSolutionsInPercent: BigDecimal? = null,
)
