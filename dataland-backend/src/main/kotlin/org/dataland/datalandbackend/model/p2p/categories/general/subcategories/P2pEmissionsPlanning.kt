package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding emissions planning
 */
data class P2pEmissionsPlanning(
    val reductionOfAbsoluteEmissions: BigDecimal? = null,

    val absoluteEmissions: BigDecimal? = null,

    val climateActionPlan: YesNo? = null,

    val reductionOfRelativeEmissions: BigDecimal? = null,

    val relativeEmissions: BigDecimal? = null,

    val useOfInternalCarbonPrice: YesNo? = null,
)
