package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Certifications, policies & responsibilities"
 */
data class LksgCertificationsPoliciesAndResponsibilities(
    val sa8000Certification: BaseDataPoint<YesNo>?,

    val smetaSocialAuditConcept: YesNo?,

    val betterWorkProgramCertificate: BaseDataPoint<YesNo>?,

    val iso45001Certification: BaseDataPoint<YesNo>?,

    val iso14000Certification: BaseDataPoint<YesNo>?,

    val emasCertification: BaseDataPoint<YesNo>?,

    val iso37001Certification: BaseDataPoint<YesNo>?,

    val iso37301Certification: BaseDataPoint<YesNo>?,

    val riskManagementSystemCertification: BaseDataPoint<YesNo>?,

    val amforiBsciAuditReport: BaseDataPoint<YesNo>?,

    val responsibleBusinessAssociationCertification: BaseDataPoint<YesNo>?,

    val fairLaborAssociationCertification: BaseDataPoint<YesNo>?,

    val additionalAudits: String?,

    val codeOfConduct: YesNo?,

    val codeOfConductTraining: YesNo?,

    val supplierCodeOfConduct: BaseDataPoint<YesNo>?,

    val policyStatement: YesNo?,

    val humanRightsStrategy: String?,

    val environmentalImpactPolicy: BaseDataPoint<YesNo>?,

    val fairWorkingConditionsPolicy: BaseDataPoint<YesNo>?,
)
