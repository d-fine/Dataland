package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Risk management - Own operations"
 */
data class LksgRiskManagementOwnOperations(
    val adequateAndEffectiveRiskManagementSystem: YesNo?,

    val riskManagementSystemFiscalYear: YesNo?,

    val riskManagementSystemRisks: YesNo?,

    val riskManagementSystemIdentifiedRisks: List<String>?,

    val riskManagementSystemCounteract: YesNo?,

    val riskManagementSystemMeasures: List<String>?,

    val riskManagementSystemResponsibility: YesNo?,

    val environmentalManagementSystem: YesNo?,

    val environmentalManagementSystemInternationalCertification: YesNo?,

    val environmentalManagementSystemNationalCertification: YesNo?,
)
