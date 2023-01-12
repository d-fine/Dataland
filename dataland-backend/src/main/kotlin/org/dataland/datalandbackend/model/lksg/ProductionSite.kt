package org.dataland.datalandbackend.model.lksg

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class ProductionSite(
    val name: String? = null,

    @field:JsonProperty()
    val isInHouseProductionOrIsContractProcessing: YesNo? = null,

    val address: String? = null,

    // TODO should this be production site associated? See data dictionary
    val listOfGoodsAndServices: List<String>? = null
)
