package org.dataland.datalandbackend.model.p2p.categories.cement.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the materials of the cement sector
*/
data class P2pCementMaterial(
    val clinkerFactorReduction: BigDecimal? = null,

    val preCalcinedClayUsage: BigDecimal? = null,

    val circularEconomyContribution: YesNo? = null,
)
