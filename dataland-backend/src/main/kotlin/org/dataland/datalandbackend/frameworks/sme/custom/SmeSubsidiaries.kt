package org.dataland.datalandbackend.frameworks.sme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.generics.Address

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class SmeSubsidiaries(
    val nameOfSubsidiary: String?,

    @field:JsonProperty(required = true)
    val addressOfSubsidiary: Address,
// TODO can and should we use the productionSite interface here, from the structure it is the same
)
