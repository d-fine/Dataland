package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Production Sites for LKSG framework
 */
data class ProductionSite(
    val location: String? = null,

    val isInHouseProductionOrIsContractProcessing: YesNo? = null,

    val addressesOfForeignProductionSites: String? = null
)
