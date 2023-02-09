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

    val listOfGoodsOrServices: List<String>? = null,
)
