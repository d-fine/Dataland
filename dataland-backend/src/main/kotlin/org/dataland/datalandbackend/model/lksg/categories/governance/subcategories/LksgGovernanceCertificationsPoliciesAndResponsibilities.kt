package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the subcategory "Certifications, policies and responsibilities" belonging to the category "Governance"
 * of the lksg framework.
*/
data class LksgGovernanceCertificationsPoliciesAndResponsibilities(
      val sa8000Certification: BaseDataPoint<YesNo>? = null,

      val smetaSocialAuditConcept: YesNo? = null,

      val betterWorkProgramCertificate: BaseDataPoint<YesNoNa>? = null,

      val iso45001Certification: BaseDataPoint<YesNo>? = null,

      val iso14001Certification: BaseDataPoint<YesNo>? = null,

      val emasCertification: BaseDataPoint<YesNo>? = null,

      val iso37001Certification: BaseDataPoint<YesNo>? = null,

      val iso37301Certification: BaseDataPoint<YesNo>? = null,

      val riskManagementSystemCertification: BaseDataPoint<YesNo>? = null,

      val amforiBsciAuditReport: BaseDataPoint<YesNo>? = null,

      val responsibleBusinessAssociationCertification: BaseDataPoint<YesNo>? = null,

      val fairLaborAssociationCertification: BaseDataPoint<YesNo>? = null,

      val additionalAudits: String? = null,

      val codeOfConduct: YesNo? = null,

      val codeOfConductTraining: YesNo? = null,

      val supplierCodeOfConduct: BaseDataPoint<YesNo>? = null,

      val policyStatement: YesNo? = null,

      val humanRightsStrategy: String? = null,

      val environmentalImpactPolicy: BaseDataPoint<YesNo>? = null,

      val fairWorkingConditionsPolicy: BaseDataPoint<YesNo>? = null,
)
