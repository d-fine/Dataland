package org.dataland.datalandbackend.frameworks.heimathafen.model.governance

import org.dataland.datalandbackend.frameworks.heimathafen.model.governance.goodGovernance.HeimathafenGovernanceGoodGovernance
import org.dataland.datalandbackend.frameworks.heimathafen.model.governance.kontroverseGeschaeftsfelder.HeimathafenGovernanceKontroverseGeschaeftsfelder

/**
 * The data-model for the Governance section
 */
data class HeimathafenGovernance(
    val goodGovernance: HeimathafenGovernanceGoodGovernance?,
    val kontroverseGeschaeftsfelder: HeimathafenGovernanceKontroverseGeschaeftsfelder?,
)
