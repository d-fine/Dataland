package org.dataland.datalandbackend.model.p2p.categories.hvcPlastics

import org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories.P2pHvcPlasticsDecarbonisation
import org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories.P2pHvcPlasticsDefossilisation
import org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.subcategories.P2pHvcPlasticsRecycling

/**
* --- API model ---
* Fields of the P2P questionnaire regarding the HVC Plastics sector
*/
data class P2pHvcPlastics(
    val decarbonisation: P2pHvcPlasticsDecarbonisation?,

    val defossilisation: P2pHvcPlasticsDefossilisation?,

    val recycling: P2pHvcPlasticsRecycling?,
)
