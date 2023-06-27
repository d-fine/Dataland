package org.dataland.datalandbackend.model.p2p.categories.realEstate.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the used technology in the real estate sector
 */
data class P2plRealEstateTechnology(
    val useOfDistrictHeatingNetworks: YesNo?,

    val heatPumpUsage: YesNo?,
)
