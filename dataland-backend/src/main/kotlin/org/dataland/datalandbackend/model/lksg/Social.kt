package org.dataland.datalandbackend.model.lksg

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Social"
 */
data class Social(
    val general: General? = null,
    val grievanceMechanism: GrievanceMechanism? = null,
    val childLabour: ChildLabour? = null,
    val forcedLabourSlaveryAndDebtBondage: ForcedLabourSlaveryAndDebtBondage? = null,
    val osh: SocialOsh? = null,
    val freedomOfAssociation: FreedomOfAssociation? = null,
    val humanRights: HumanRights? = null,
    val evidenceCertificatesAndAttestations: EvidenceCertificatesAndAttestations? = null,
)