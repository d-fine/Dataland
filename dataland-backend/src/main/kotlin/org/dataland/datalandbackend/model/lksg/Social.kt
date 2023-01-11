package org.dataland.datalandbackend.model.lksg

data class Social(
    val general: General,
    val grievanceMechanism: GrievanceMechanism,
    val childLabour: ChildLabour,
    val forcedLabourSlaveryAndDebtBondage: ForcedLabourSlaveryAndDebtBondage,
    val osh: SocialOsh,
    val freedomOfAssociation: FreedomOfAssociation,
    val humanRights: HumanRights,
    val evidenceCertificatesAndAttestations: EvidenceCertificatesAndAttestations,

    )