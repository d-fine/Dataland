package org.dataland.datalandbackend.frameworks.heimathafen.model

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.HeimathafenGeneral

/**
 * The root data-model for the Heimathafen Framework
 */
@DataType("heimathafen")
data class HeimathafenData(
    val general: HeimathafenGeneral?,
)
