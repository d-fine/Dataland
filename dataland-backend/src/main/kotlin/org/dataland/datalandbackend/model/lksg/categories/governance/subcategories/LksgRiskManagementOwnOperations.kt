package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Risk management - Own operations"
 */
data class LksgRiskManagementOwnOperations(
    val adequateAndEffectiveRiskManagementSystem: YesNo?,

    val riskManagementSystemFiscalYear: YesNo?,

    val riskManagementSystemRisks: YesNo?,

    val riskManagementSystemIdentifiedRisks: String?,

    val riskManagementSystemCounteract: YesNo?,

    val riskManagementSystemMeasures: String?,

    val riskManagementSystemResponsibility: YesNo?,

    val environmentalManagementSystem: YesNo?,

    val environmentalManagementSystemInternationalCertification: BaseDataPoint<YesNo>?,

    val environmentalManagementSystemNationalCertification: BaseDataPoint<YesNo>?,
)
