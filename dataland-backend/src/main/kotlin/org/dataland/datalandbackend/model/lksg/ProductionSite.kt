package org.dataland.datalandbackend.model.lksg

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Production Sites for LKSG framework
 */
data class ProductionSite(
    val name: String? = null,

    @field:JsonProperty()
    val isInHouseProductionOrIsContractProcessing: YesNo? = null,

    val address: String? = null
)

