package org.dataland.datalandbackend.model.p2p.categories.livestockFarming.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the animal feed of the livestock farming sector
*/
data class P2pLivestockFarmingAnimalFeed(
    val ownFeedPercentage: BigDecimal?,

    val externalFeedCertification: BaseDataPoint<YesNo?>,

    val originOfExternalFeed: String?,

    val excessNitrogen: BigDecimal?,

    val cropRotation: BigDecimal?,

    val climateFriendlyProteinProduction: BigDecimal?,

    val greenFodderPercentage: BigDecimal?,
)
