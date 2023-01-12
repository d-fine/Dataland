package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.ChildLabour
import org.dataland.datalandbackend.model.lksg.submodels.EvidenceCertificatesAndAttestations
import org.dataland.datalandbackend.model.lksg.submodels.ForcedLabourSlaveryAndDebtBondage
import org.dataland.datalandbackend.model.lksg.submodels.FreedomOfAssociation
import org.dataland.datalandbackend.model.lksg.submodels.GrievanceMechanism
import org.dataland.datalandbackend.model.lksg.submodels.LksgGeneral
import org.dataland.datalandbackend.model.lksg.submodels.LksgHumanRights
import org.dataland.datalandbackend.model.lksg.submodels.SocialOsh

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
