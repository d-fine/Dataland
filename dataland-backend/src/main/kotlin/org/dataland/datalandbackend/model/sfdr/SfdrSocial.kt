package org.dataland.datalandbackend.model.sfdr

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrAnticorruptionAndAntibribery
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrGeneral
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrGreenSecurities
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrHumanRights
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrSocialAndEmployeeMatters

/**
 * --- API model ---
 * Impact topics for the "Social" impact area of the SFDR framework
 */
data class SfdrSocial(
    @field:JsonProperty(required = true)
    val general: SfdrGeneral,

    val socialAndEmployeeMatters: SfdrSocialAndEmployeeMatters?,

    val greenSecurities: SfdrGreenSecurities?,

    val humanRights: SfdrHumanRights?,

    val anticorruptionAndAntibribery: SfdrAnticorruptionAndAntibribery?,
)
