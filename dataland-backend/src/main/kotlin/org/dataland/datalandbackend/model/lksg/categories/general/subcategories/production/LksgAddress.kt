package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Addresses for LkSG framework
 */
data class LksgAddress(
    val streetAndHouseNumber: String?,

    val postalCode: String?,

    @field:JsonProperty(required = true)
    val city: String,

    val state: String?,

    @field:JsonProperty(required = true)
    val country: String,
)
