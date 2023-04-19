package org.dataland.datalandbackend.model.lksg.categories.governance

import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories.LksgEvidenceCertificatesAndAttestations
import org.dataland.datalandbackend.model.lksg.categories.governance.subcategories.LksgHumanRights

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Governance"
 */
data class LksgGovernance(
    val evidenceCertificatesAndAttestations: LksgEvidenceCertificatesAndAttestations?,

    val humanRights: LksgHumanRights?,
)
