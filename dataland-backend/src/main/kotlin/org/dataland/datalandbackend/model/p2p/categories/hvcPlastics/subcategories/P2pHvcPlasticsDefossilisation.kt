package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the defossilisation of the HVC Plastics sector
 */
data class P2pHvcPlasticsDefossilisation(
    val useOfRenewableFeedstocksInPercent: BigDecimal? = null,

    val useOfBioplasticsInPercent: BigDecimal? = null,

    val useOfCo2FromCarbonCaptureAndReUseTechnologiesInPercent: BigDecimal? = null,

    val carbonCaptureAndUseStorageTechnologies: BigDecimal? = null,
)
