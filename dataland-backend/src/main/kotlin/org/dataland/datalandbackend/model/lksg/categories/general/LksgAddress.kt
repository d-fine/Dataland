package org.dataland.datalandbackend.model.lksg.categories.general

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Addresses for LkSG framework
 */
data class LksgAddress(
    @field:JsonProperty(required = true)
    val streetAndHouseNumber: String,

    @field:JsonProperty(required = true)
    val city: String,

    @field:JsonProperty(required = true)
    val state: String,

    @field:JsonProperty(required = true)
    val postalCode: String,

    @field:JsonProperty(required = true)
    val country: String,
)
