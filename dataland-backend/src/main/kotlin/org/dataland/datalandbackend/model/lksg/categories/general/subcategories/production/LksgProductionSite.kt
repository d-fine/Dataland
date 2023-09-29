package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.ProductionSiteInterface
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
) : ProductionSiteInterface
