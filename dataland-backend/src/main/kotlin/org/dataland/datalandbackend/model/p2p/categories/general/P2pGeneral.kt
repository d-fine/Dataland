package org.dataland.datalandbackend.model.p2p.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pClimateTargets
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pEmissionsPlanning
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGeneralGeneral
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGovernance
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pInvestmentPlanning

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding general information applicable to companies of all sectors.
 */
data class P2pGeneral(
    @field:JsonProperty(required = true)
    val general: P2pGeneralGeneral,

    val governance: P2pGovernance? = null,

    val climateTargets: P2pClimateTargets? = null,

    val emissionsPlanning: P2pEmissionsPlanning? = null,

    val investmentPlanning: P2pInvestmentPlanning? = null,
)
