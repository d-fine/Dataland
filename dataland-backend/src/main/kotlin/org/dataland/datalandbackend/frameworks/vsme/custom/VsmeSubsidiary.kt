package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.generics.Address

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class VsmeSubsidiary(
    @field:JsonProperty(required = true)
    val nameOfSubsidiary: String,
    @field:JsonProperty(required = true)
    val addressOfSubsidiary: Address,
)
