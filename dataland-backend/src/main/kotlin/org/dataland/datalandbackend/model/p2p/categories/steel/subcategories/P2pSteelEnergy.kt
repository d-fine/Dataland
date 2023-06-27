package org.dataland.datalandbackend.model.p2p.categories.steel.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the energy of the steel sector
*/
data class P2pSteelEnergy(
    val emissionIntensityOfElectricity: BigDecimal?,

    val greenHydrogenUsage: YesNo?,
)
