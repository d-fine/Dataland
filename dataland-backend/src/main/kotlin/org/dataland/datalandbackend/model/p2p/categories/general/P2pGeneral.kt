package org.dataland.datalandbackend.model.p2p.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGeneralClimateTargets
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGeneralEmissionsPlanning
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGeneralGeneral
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGeneralGovernance
import org.dataland.datalandbackend.model.p2p.categories.general.subcategories.P2pGeneralInvestmentPlanning

/**
 * --- API model ---
 * Fields of the category "General" of the p2p framework.
*/
data class P2pGeneral(
    @field:JsonProperty(required = true)
    val general: P2pGeneralGeneral,
    @field:Valid
    val governance: P2pGeneralGovernance? = null,
    val climateTargets: P2pGeneralClimateTargets? = null,
    val emissionsPlanning: P2pGeneralEmissionsPlanning? = null,
    val investmentPlanning: P2pGeneralInvestmentPlanning? = null,
)
