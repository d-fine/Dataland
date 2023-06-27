package org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories

import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the materials of the automotive sector
*/
data class P2pAutomotiveMaterials(
    val materialUseManagement: BigDecimal?,

    val useOfSecondaryMaterials: BigDecimal?,
)
