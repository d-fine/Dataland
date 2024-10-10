package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Defossilisation" belonging to the category "HVC Plastics" of the p2p framework.
*/
data class P2pHvcPlasticsDefossilisation(
    val useOfRenewableFeedstocksInPercent: BigDecimal? = null,
    val useOfBioplasticsInPercent: BigDecimal? = null,
    val useOfCo2FromCarbonCaptureAndReUseTechnologiesInPercent: BigDecimal? = null,
    val carbonCaptureAndUseStorageTechnologies: YesNo? = null,
)
