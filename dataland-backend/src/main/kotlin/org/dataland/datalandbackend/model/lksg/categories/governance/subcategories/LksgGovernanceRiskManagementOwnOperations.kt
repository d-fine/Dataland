package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.BaseDataPoint

/**
 * --- API model ---
 * Fields of the subcategory "Risk management - Own Operations" belonging to the category "Governance" of the
 * Lksg framework.
*/
data class LksgGovernanceRiskManagementOwnOperations(
      val riskManagementSystem: YesNo? = null,

      val riskManagementSystemFiscalYear: YesNo? = null,

      val riskManagementSystemRisks: YesNo? = null,

      val riskManagementSystemIdentifiedRisks: String? = null,

      val riskManagementSystemCounteract: YesNo? = null,

      val riskManagementSystemMeasures: String? = null,

      val riskManagementSystemResponsibility: YesNo? = null,

      val environmentalManagementSystem: YesNo? = null,

      val environmentalManagementSystemInternationalCertification: BaseDataPoint<YesNo>? = null,

      val environmentalManagementSystemNationalCertification: BaseDataPoint<YesNo>? = null,
)
