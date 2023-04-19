package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Evidence, certificates and attestations"
 */
data class LksgEvidenceCertificatesAndAttestations(
    @field:JsonProperty(required = true)
    val sa8000Certification: YesNo,

    @field:JsonProperty(required = true)
    val smetaSocialAuditConcept: YesNo,

    @field:JsonProperty(required = true)
    val betterWorkProgramCertificate: YesNo,

    @field:JsonProperty(required = true)
    val iso45001Certification: YesNo,

    @field:JsonProperty(required = true)
    val iso14000Certification: YesNo,

    @field:JsonProperty(required = true)
    val emasCertification: YesNo,

    @field:JsonProperty(required = true)
    val iso37001Certification: YesNo,

    @field:JsonProperty(required = true)
    val iso37301Certification: YesNo,

    @field:JsonProperty(required = true)
    val riskManagementSystemCertification: YesNo,

    @field:JsonProperty(required = true)
    val amforiBsciAuditReport: YesNo,

    @field:JsonProperty(required = true)
    val responsibleBusinessAssociationCertification: YesNo,

    @field:JsonProperty(required = true)
    val fairLaborAssociationCertification: YesNo,

    val additionalAudits: List<String>?,
)
