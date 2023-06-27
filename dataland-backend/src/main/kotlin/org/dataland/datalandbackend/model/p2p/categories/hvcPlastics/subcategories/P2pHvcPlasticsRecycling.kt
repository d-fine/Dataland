package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding recycling in the HVC Plastics sector
 */
data class P2pHvcPlasticsRecycling(
    val contributionToCircularEconomy: YesNo? = null,

    val materialRecycling: BigDecimal? = null,

    val chemicalRecycling: BigDecimal? = null,
)
