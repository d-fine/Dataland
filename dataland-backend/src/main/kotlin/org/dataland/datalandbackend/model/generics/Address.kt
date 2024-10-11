package org.dataland.datalandbackend.model.generics

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Addresses for LkSG framework
 */
data class Address(
    val streetAndHouseNumber: String?,
    val postalCode: String?,
    @field:JsonProperty(required = true)
    val city: String,
    val state: String?,
    @field:JsonProperty(required = true)
    val country: String,
)
