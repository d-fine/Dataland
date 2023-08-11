package org.dataland.datalandbackend.model.sfdr.categories.social

import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialAntiCorruptionAndAntiBribery
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialGreenSecurities
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialHumanRights
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialSocialAndEmployeeMatters

/**
 * --- API model ---
 * Fields of the category "Social" of the sfdr framework.
 */
data class SfdrSocial(
    val socialAndEmployeeMatters: SfdrSocialSocialAndEmployeeMatters? = null,

    val greenSecurities: SfdrSocialGreenSecurities? = null,

    val humanRights: SfdrSocialHumanRights? = null,

    val antiCorruptionAndAntiBribery: SfdrSocialAntiCorruptionAndAntiBribery? = null,
)
