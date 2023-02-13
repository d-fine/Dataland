package org.dataland.datalandbackend.model.lksg

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.lksg.InHouseProductionOrContractProcessing

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class ProductionSite(
    val name: String? = null,

    @field:JsonProperty()
    val isInHouseProductionOrIsContractProcessing: InHouseProductionOrContractProcessing? = null,

    val country: String? = null,

    val city: String? = null,

    val streetAndHouseNumber: String? = null,

    val postalCode: String? = null,

    val listOfGoodsOrServices: List<String>? = null,
)
