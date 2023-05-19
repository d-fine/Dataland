package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class LksgProductionSite(
    val nameOfProductionSite: String?,

    val addressOfProductionSite: LksgAddress?,

    val listOfGoodsOrServices: List<String>?,
)
