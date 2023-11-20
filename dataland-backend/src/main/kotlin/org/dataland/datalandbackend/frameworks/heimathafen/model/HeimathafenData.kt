package org.dataland.datalandbackend.frameworks.heimathafen.model

import org.dataland.datalandbackend.frameworks.heimathafen.model.general.HeimathafenGeneral
import org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.HeimathafenEnvironmental
import org.dataland.datalandbackend.frameworks.heimathafen.model.social.HeimathafenSocial
import org.dataland.datalandbackend.frameworks.heimathafen.model.governance.HeimathafenGovernance
import org.dataland.datalandbackend.annotations.DataType

/**
 * The root data-model for the Heimathafen Framework
 */
@DataType("heimathafen")
data class HeimathafenData(
    val general: HeimathafenGeneral?,
    val environmental: HeimathafenEnvironmental?,
    val social: HeimathafenSocial?,
    val governance: HeimathafenGovernance?,
)
