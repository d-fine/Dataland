package org.dataland.datalandbackend.frameworks.sme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.generics.Address

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class SmeSubsidiary(
    val nameOfSubsidiary: String?,

    @field:JsonProperty(required = true)
    val addressOfSubsidiary: Address,
// TODO can and should we use the productionSite interface here, from the structure it is the same
)
