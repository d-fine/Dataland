package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.generics.Address

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class VsmeSubsidiary(
    val nameOfSubsidiary: String?,

    @field:JsonProperty(required = true)
    val addressOfSubsidiary: Address,
// TODO can and should we use the productionSite interface here, from the structure it is the same
    // TODO check nullable of fields
)
