// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.lksg.model.governance.riskManagementOwnOperations

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.lksg.custom.LksgRiskOrViolationAssessment
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the RiskManagementOwnOperations section
 */
data class LksgGovernanceRiskManagementOwnOperations(
    @field:Valid()
    val riskManagementSystem: BaseDataPoint<YesNo>? = null,
    val riskAnalysisInFiscalYear: YesNo? = null,
    val risksIdentified: YesNo? = null,
    val identifiedRisks: List<LksgRiskOrViolationAssessment?>? = null,
    val regulatedRiskManagementResponsibility: YesNo? = null,
)
