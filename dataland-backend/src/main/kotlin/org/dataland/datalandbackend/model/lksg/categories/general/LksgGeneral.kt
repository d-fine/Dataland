package org.dataland.datalandbackend.model.lksg.categories.general

import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgGrievanceMechanismOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgMasterData
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgProductionSpecific
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgProductionSpecificOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgRiskManagementOwnOperations

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "General"
 */
data class LksgGeneral(
    val masterData: LksgMasterData?,

    val productionSpecific: LksgProductionSpecific?,

    val productionSpecificOwnOperations: LksgProductionSpecificOwnOperations?,

    val riskManagementOwnOperations: LksgRiskManagementOwnOperations?,

    val grievanceMechanismOwnOperations: LksgGrievanceMechanismOwnOperations?,
)
