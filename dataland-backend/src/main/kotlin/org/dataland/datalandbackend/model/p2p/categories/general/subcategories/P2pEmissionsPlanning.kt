package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding emissions planning
 */
data class P2pEmissionsPlanning(
    val absoluteEmissions: BigDecimal? = null,

    val relativeEmissionsInPercent: BigDecimal? = null,

    val reductionOfAbsoluteEmissions: BigDecimal? = null,

    val reductionOfRelativeEmissionsInPercent: BigDecimal? = null,

    val climateActionPlan: YesNo? = null,

    val useOfInternalCarbonPrice: YesNo? = null,
)
