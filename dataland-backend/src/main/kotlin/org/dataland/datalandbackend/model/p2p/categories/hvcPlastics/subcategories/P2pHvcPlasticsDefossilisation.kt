package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the defossilisation of the HVC Plastics sector
 */
data class P2pHvcPlasticsDefossilisation(
    val useOfRenewableFeedstocks: BigDecimal? = null,

    val useOfBioplastics: BigDecimal? = null,

    val useOfCo2FromCarbonCaptureAndReUseTechnologies: BigDecimal? = null,

    val carbonCaptureAndUseStorageTechnologies: BigDecimal? = null,
)
