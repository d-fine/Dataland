package org.dataland.datalandbackend.model.lksg.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgGrievanceMechanismOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgMasterData
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgProductionspecific
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgProductionspecificOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgRiskManagementOwnOperations

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "General"
 */
data class LksgGeneral(
    val masterData: LksgMasterData?,

    @field:JsonProperty(required = true)
    val productionspecific: LksgProductionspecific,

    val productionspecificOwnOperations: LksgProductionspecificOwnOperations?,

    val riskManagementOwnOperations: LksgRiskManagementOwnOperations?,

    val grievanceMechanismOwnOperations: LksgGrievanceMechanismOwnOperations?,
)
