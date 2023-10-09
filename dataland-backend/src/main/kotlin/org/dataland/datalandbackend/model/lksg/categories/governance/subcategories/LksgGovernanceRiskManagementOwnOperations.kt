package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Risk management - Own Operations" belonging to the category "Governance" of the
 * lksg framework.
*/
data class LksgGovernanceRiskManagementOwnOperations(
    val riskManagementSystem: YesNo? = null,

    val riskAnalysisInFiscalYear: YesNo? = null,

    val risksIdentified: YesNo? = null,

    val identifiedRisks: String? = null,

    val counteractingMeasures: YesNo? = null,

    val whichCounteractingMeasures: String? = null,

    val regulatedRiskManagementResponsibility: YesNo? = null,

    val environmentalManagementSystem: YesNo? = null,

    val environmentalManagementSystemInternationalCertification: BaseDataPoint<YesNo>? = null,

    val environmentalManagementSystemNationalCertification: BaseDataPoint<YesNo>? = null,
)
