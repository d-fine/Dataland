// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.lksg.model.general

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.lksg.model.general.masterData.LksgGeneralMasterData
import org.dataland.datalandbackend.frameworks.lksg.model.general.productionSpecific.LksgGeneralProductionSpecific
import org.dataland.datalandbackend.frameworks.lksg.model.general.productionSpecificOwnOperations
    .LksgGeneralProductionSpecificOwnOperations

/**
 * The data-model for the General section
 */
@Suppress("MaxLineLength")
data class LksgGeneral(
    @field:Valid()
    val masterData: LksgGeneralMasterData,
    @field:Valid()
    val productionSpecific: LksgGeneralProductionSpecific? = null,
    @field:Valid()
    val productionSpecificOwnOperations: LksgGeneralProductionSpecificOwnOperations? = null,
)
