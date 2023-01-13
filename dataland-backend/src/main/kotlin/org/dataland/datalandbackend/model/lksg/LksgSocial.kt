package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.LksgChildLabour
import org.dataland.datalandbackend.model.lksg.submodels.LksgEvidenceCertificatesAndAttestations
import org.dataland.datalandbackend.model.lksg.submodels.LksgForcedLabourSlaveryAndDebtBondage
import org.dataland.datalandbackend.model.lksg.submodels.LksgFreedomOfAssociation
import org.dataland.datalandbackend.model.lksg.submodels.LksgGrievanceMechanism
import org.dataland.datalandbackend.model.lksg.submodels.LksgGeneral
import org.dataland.datalandbackend.model.lksg.submodels.LksgHumanRights
import org.dataland.datalandbackend.model.lksg.submodels.LksgSocialOsh

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Social"
 */
data class LksgSocial(
    val general: LksgGeneral?,
    val grievanceMechanism: LksgGrievanceMechanism?,
    val childLabour: LksgChildLabour?,
    val forcedLabourSlaveryAndDebtBondage: LksgForcedLabourSlaveryAndDebtBondage?,
    val osh: LksgSocialOsh?,
    val freedomOfAssociation: LksgFreedomOfAssociation?,
    val humanRights: LksgHumanRights?,
    val evidenceCertificatesAndAttestations: LksgEvidenceCertificatesAndAttestations?,
)
