package org.dataland.datalandbackend.model.lksg.categories.general

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.lksg.LksgProductionSite
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgGrievanceMechanismOwnOperations
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgMasterData
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.LksgRiskManagementOwnOperations
import java.math.BigDecimal

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "General"
 */
data class LksgGeneral(
    val masterData: LksgMasterData?,

    val manufacturingCompany: YesNo?,

    val numberOfProductionSites: BigDecimal?,

    val listOfProductionSites: List<LksgProductionSite>?,

    val riskManagementOwnOperations: LksgRiskManagementOwnOperations?,

    val grievanceMechanismOwnOperations: LksgGrievanceMechanismOwnOperations?,
)
