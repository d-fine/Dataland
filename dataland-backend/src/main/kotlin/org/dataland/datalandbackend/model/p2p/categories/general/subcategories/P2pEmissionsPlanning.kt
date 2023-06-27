package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding emissions planning
 */
data class P2pEmissionsPlanning(
    val reductionOfAbsoluteEmissions: BigDecimal?,

    val absoluteEmissions: BigDecimal?,

    val climateActionPlan: YesNo?,

    val reductionOfRelativeEmissions: BigDecimal?,

    val relativeEmissions: BigDecimal?,

    val useOfInternalCarbonPrice: YesNo?,
)
