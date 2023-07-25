package org.dataland.datalandbackend.model.sfdr.categories.social

import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialGeneral
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialSocialAndEmployeeMatters
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialGreenSecurities
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialHumanRights
import org.dataland.datalandbackend.model.sfdr.categories.social.subcategories.SfdrSocialAntiCorruptionAndAntiBribery
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of the category "Social" of the sfdr framework.
 */
data class SfdrSocial(
    @field:JsonProperty(required = true)
    val general: SfdrSocialGeneral,

    val socialAndEmployeeMatters: SfdrSocialSocialAndEmployeeMatters? = null,

    val greenSecurities: SfdrSocialGreenSecurities? = null,

    val humanRights: SfdrSocialHumanRights? = null,

    val antiCorruptionAndAntiBribery: SfdrSocialAntiCorruptionAndAntiBribery? = null,
)
