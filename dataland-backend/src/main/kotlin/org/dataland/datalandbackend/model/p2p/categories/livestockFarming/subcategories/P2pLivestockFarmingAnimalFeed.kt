package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Animal feed" belonging to the category "Livestock farming" of the p2p framework.
*/
data class P2pLivestockFarmingAnimalFeed(
    val ownFeedInPercent: BigDecimal? = null,

    val externalFeedCertification: BaseDataPoint<YesNo>? = null,

    val originOfExternalFeed: String? = null,

    val excessNitrogenInKilogramsPerHectare: BigDecimal? = null,

    val cropRotation: BigDecimal? = null,

    val climateFriendlyProteinProductionInPercent: BigDecimal? = null,

    val greenFodderInPercent: BigDecimal? = null,
)
