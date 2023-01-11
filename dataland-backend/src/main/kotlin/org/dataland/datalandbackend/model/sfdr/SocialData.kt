package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.model.sfdr.submodels.AnticorruptionAndAntibribery
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrGeneral
import org.dataland.datalandbackend.model.sfdr.submodels.GreenSecurities
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrHumanRights
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrSocialAndEmployeeMatters

/**
 * --- API model ---
 * Impact topics for the "Social" impact area of the SFDR framework
 */
data class SocialData(
    val general: SfdrGeneral? = null,

    val socialAndEmployeeMatters: SfdrSocialAndEmployeeMatters? = null,

    val greenSecurities: GreenSecurities? = null,

    val humanRights: SfdrHumanRights? = null,

    val anticorruptionAndAntibribery: AnticorruptionAndAntibribery? = null,
)
