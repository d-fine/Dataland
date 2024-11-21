package org.dataland.datalandbackend.frameworks.lksg.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.frameworks.ProductionSite
import org.dataland.datalandbackend.model.generics.Address

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class LksgProductionSite(
    override val nameOfProductionSite: String?,
    @field:JsonProperty(required = true)
    override val addressOfProductionSite: Address,
    val listOfGoodsOrServices: List<String>?,
) : ProductionSite
