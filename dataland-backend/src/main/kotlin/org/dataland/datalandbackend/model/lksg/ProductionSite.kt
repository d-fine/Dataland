package org.dataland.datalandbackend.model.lksg

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.lksg.InHouseProductionOrContractProcessing

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class ProductionSite(
    @field:JsonProperty(required = true)
    val name: String,

    @field:JsonProperty(required = true)
    val isInHouseProductionOrIsContractProcessing: InHouseProductionOrContractProcessing,

    @field:JsonProperty(required = true)
    val country: String,

    @field:JsonProperty(required = true)
    val city: String,

    @field:JsonProperty(required = true)
    val streetAndHouseNumber: String,

    @field:JsonProperty(required = true)
    val postalCode: String,

    val listOfGoodsOrServices: List<String>? = null,
)
