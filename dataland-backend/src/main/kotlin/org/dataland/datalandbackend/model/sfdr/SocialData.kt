package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.model.sfdr.submodels.AnticorruptionAndAntibribery
import org.dataland.datalandbackend.model.sfdr.submodels.General
import org.dataland.datalandbackend.model.sfdr.submodels.GreenSecurities
import org.dataland.datalandbackend.model.sfdr.submodels.HumanRights
import org.dataland.datalandbackend.model.sfdr.submodels.SocialAndEmployeeMatters

/**
 * --- API model ---
 * Impact topics for the "Social" impact area of the SFDR framework
 */
data class SocialData(
    val general: General? = null,

    val socialAndEmployeeMatters: SocialAndEmployeeMatters? = null,

    val greenSecurities: GreenSecurities? = null,

    val humanRights: HumanRights? = null,

    val anticorruptionAndAntibribery: AnticorruptionAndAntibribery? = null,
)
