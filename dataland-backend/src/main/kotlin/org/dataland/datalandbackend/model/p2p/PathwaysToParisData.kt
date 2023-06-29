package org.dataland.datalandbackend.model.p2p

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.p2p.categories.ammonia.P2pAmmonia
import org.dataland.datalandbackend.model.p2p.categories.automotive.P2pAutomotive
import org.dataland.datalandbackend.model.p2p.categories.cement.P2pCement
import org.dataland.datalandbackend.model.p2p.categories.electricityGeneration.P2pElectricityGeneration
import org.dataland.datalandbackend.model.p2p.categories.freightTransportByRoad.P2pFreightTransportByRoad
import org.dataland.datalandbackend.model.p2p.categories.general.P2pGeneral
import org.dataland.datalandbackend.model.p2p.categories.hvcPlastics.P2pHvcPlastics
import org.dataland.datalandbackend.model.p2p.categories.livestockFarming.P2pLivestockFarming
import org.dataland.datalandbackend.model.p2p.categories.realEstate.P2pRealEstate
import org.dataland.datalandbackend.model.p2p.categories.steel.P2pSteel

/**
 * --- API model ---
 * The Pathways to Paris (P2P) Framework Questionaire
 */
@DataType("p2p")
data class PathwaysToParisData(
    @field:JsonProperty(required = true)
    val general: P2pGeneral,

    val ammonia: P2pAmmonia? = null,

    val automotive: P2pAutomotive? = null,

    val hvcPlastics: P2pHvcPlastics? = null,

    val commercialRealEstate: P2pRealEstate? = null,

    val residentialRealEstate: P2pRealEstate? = null,

    val steel: P2pSteel? = null,

    val freightTransportByRoad: P2pFreightTransportByRoad? = null,

    val electricityGeneration: P2pElectricityGeneration? = null,

    val livestockFarming: P2pLivestockFarming? = null,

    val cement: P2pCement? = null,
)
