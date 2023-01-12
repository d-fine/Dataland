package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "OSH" of the impact area "Social"
 */
data class SocialOsh(
    val oshMonitoring: YesNo? = null,

    val oshPolicy: YesNo? = null,

    val oshPolicyPersonalProtectiveEquipment: YesNo? = null,

    val oshPolicyMachineSafety: YesNo? = null,

    val oshPolicyDisasterBehaviouralResponse: YesNo? = null,

    val oshPolicyAccidentsBehaviouralResponse: YesNo? = null,

    val oshPolicyWorkplaceErgonomics: YesNo? = null,

    val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: YesNo? = null,

    val oshPolicyFireProtection: YesNo? = null,

    val oshPolicyWorkingHours: YesNo? = null,

    val oshPolicyTrainingAddressed: YesNo? = null,

    val oshPolicyTraining: YesNo? = null,

    val oshManagementSystem: YesNo? = null,

    val oshManagementSystemInternationalCertification: YesNo? = null,

    val oshManagementSystemNationalCertification: YesNo? = null,

    val workplaceAccidentsUnder10: YesNo? = null,

    val oshTraining: YesNo? = null,
)
