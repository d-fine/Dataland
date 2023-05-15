package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Risk management - Own operations"
 */
data class LksgRiskManagementOwnOperations(
    val adequateAndEffectiveRiskManagementSystem: BaseDataPoint<YesNo>?,

    val riskManagementSystemFiscalYear: BaseDataPoint<YesNo>?,

    val riskManagementSystemRisks: BaseDataPoint<YesNo>?,

    val riskManagementSystemIdentifiedRisks: BaseDataPoint<String>?,

    val riskManagementSystemCounteract: BaseDataPoint<YesNo>?,

    val riskManagementSystemMeasures: BaseDataPoint<String>?,

    val riskManagementSystemResponsibility: BaseDataPoint<YesNo>?,

    val environmentalManagementSystem: BaseDataPoint<YesNo>?,

    val environmentalManagementSystemInternationalCertification: BaseDataPoint<YesNo>?,

    val environmentalManagementSystemNationalCertification: BaseDataPoint<YesNo>?,
)
