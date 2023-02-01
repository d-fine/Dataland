package org.dataland.datalandbackend.model.lksg

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class ProductionSite(
    val name: String? = null,

    @field:JsonProperty()
    val isInHouseProductionOrIsContractProcessing: String? = null,

    val country: String? = null,

    val city: String? = null,

    val streetAndHouseNumber: String? = null,

    val postalCode: String? = null,

    val listOfGoodsOrServices: List<String>? = null
)
