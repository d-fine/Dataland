package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class EvidenceCertificatesAndAttestations(
    val iso26000: YesNo?,

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

    val initiativeClauseSocialCertification: YesNo?,

    val responsibleBusinessAssociationCertification: YesNo?,

    val fairLabourAssociationCertification: YesNo?,

    val fairWorkingConditionsPolicy: YesNo?,

    val fairAndEthicalRecruitmentPolicy: YesNo?,

    val equalOpportunitiesAndNondiscriminationPolicy: YesNo?,

    val healthAndSafetyPolicy: YesNo?,

    val complaintsAndGrievancesPolicy: YesNo?,

    val forcedLabourPolicy: YesNo?,

    val childLabourPolicy: YesNo?,

    val environmentalImpactPolicy: YesNo?,

    val supplierCodeOfConduct: YesNo?,
)