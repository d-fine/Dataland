package org.dataland.datalandbackend.model.sfdr

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
