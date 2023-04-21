package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Evidence, certificates and attestations"
 */
data class LksgEvidenceCertificatesAndAttestations(
    val sa8000Certification: YesNo?,

    val smetaSocialAuditConcept: YesNo?,

    val betterWorkProgramCertificate: YesNo?,

    val iso45001Certification: YesNo?,

    val iso14000Certification: YesNo?,

    val emasCertification: YesNo?,

    val iso37001Certification: YesNo?,

    val iso37301Certification: YesNo?,

    val riskManagementSystemCertification: YesNo?,

    val amforiBsciAuditReport: YesNo?,

    val responsibleBusinessAssociationCertification: YesNo?,

    val fairLaborAssociationCertification: YesNo?,

    val additionalAudits: String?,
)
