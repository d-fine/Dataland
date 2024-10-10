package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Emissions planning" belonging to the category "General" of the p2p framework.
*/
data class P2pGeneralEmissionsPlanning(
    val absoluteEmissionsInTonnesCO2e: BigDecimal? = null,
    val relativeEmissionsInPercent: BigDecimal? = null,
    val reductionOfAbsoluteEmissionsInTonnesCO2e: BigDecimal? = null,
    val reductionOfRelativeEmissionsInPercent: BigDecimal? = null,
    val climateActionPlan: YesNo? = null,
    val useOfInternalCarbonPrice: YesNo? = null,
)
