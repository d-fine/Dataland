package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Evidence certificates and attestations"
 */
data class EvidenceCertificatesAndAttestations(
    val iso26000: YesNo? = null,

    val sa8000Certification: YesNo? = null,

    val smetaSocialAuditConcept: YesNo? = null,

    val betterWorkProgramCertificate: YesNo? = null,

    val iso45001Certification: YesNo? = null,

    val iso14000Certification: YesNo? = null,

    val emasCertification: YesNo? = null,

    val iso37001Certification: YesNo? = null,

    val iso37301Certification: YesNo? = null,

    val riskManagementSystemCertification: YesNo? = null,

    val amforiBsciAuditReport: YesNo? = null,

    val initiativeClauseSocialCertification: YesNo? = null,

    val responsibleBusinessAssociationCertification: YesNo? = null,

    val fairLabourAssociationCertification: YesNo? = null,

    val fairWorkingConditionsPolicy: YesNo? = null,

    val fairAndEthicalRecruitmentPolicy: YesNo? = null,

    val equalOpportunitiesAndNondiscriminationPolicy: YesNo? = null,

    val healthAndSafetyPolicy: YesNo? = null,

    val complaintsAndGrievancesPolicy: YesNo? = null,

    val forcedLabourPolicy: YesNo? = null,

    val childLabourPolicy: YesNo? = null,

    val environmentalImpactPolicy: YesNo? = null,

    val supplierCodeOfConduct: YesNo? = null,
)