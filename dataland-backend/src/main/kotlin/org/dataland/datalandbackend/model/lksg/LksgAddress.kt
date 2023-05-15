package org.dataland.datalandbackend.model.lksg

/**
 * --- API model ---
 * Addresses for LkSG framework
 */
data class LksgAddress(
    val streetAndHouseNumber: String?,

    val postalCode: String?,

    val city: String?,

    val state: String?,

    val country: String?,
)
