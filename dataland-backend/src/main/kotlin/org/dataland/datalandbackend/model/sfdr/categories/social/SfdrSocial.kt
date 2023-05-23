package org.dataland.datalandbackend.model.sfdr.categories.social

import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrAnticorruptionAndAntibribery
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrGeneral
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrGreenSecurities
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrHumanRights
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialAndEmployeeMatters

/**
 * --- API model ---
 * Impact topics for the "Social" impact area of the SFDR framework
 */
data class SfdrSocial(
    val general: SfdrGeneral?,

    val socialAndEmployeeMatters: SfdrSocialAndEmployeeMatters?,

    val greenSecurities: SfdrGreenSecurities?,

    val humanRights: SfdrHumanRights?,

    val anticorruptionAndAntibribery: SfdrAnticorruptionAndAntibribery?,
)
