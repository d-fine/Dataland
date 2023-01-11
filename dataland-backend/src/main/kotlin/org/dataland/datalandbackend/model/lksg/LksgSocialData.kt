package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.*

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Social"
 */
data class LksgSocialData(
    val general: LksgGeneral? = null,
    val grievanceMechanism: GrievanceMechanism? = null,
    val childLabour: ChildLabour? = null,
    val forcedLabourSlaveryAndDebtBondage: ForcedLabourSlaveryAndDebtBondage? = null,
    val osh: SocialOsh? = null,
    val freedomOfAssociation: FreedomOfAssociation? = null,
    val humanRights: LksgHumanRights? = null,
    val evidenceCertificatesAndAttestations: EvidenceCertificatesAndAttestations? = null,
)