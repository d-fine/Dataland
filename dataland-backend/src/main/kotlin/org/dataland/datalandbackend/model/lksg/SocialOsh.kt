package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "OSH" of the impact area "Social"
 */
data class SocialOsh(
    val oshMonitoring: YesNo?,

    val oshPolicy: YesNo?,

    val oshPolicyPersonalProtectiveEquipment: YesNo?,

    val oshPolicyMachineSafety: YesNo?,

    val oshPolicyDisasterBehaviouralResponse: YesNo?,

    val oshPolicyAccidentsBehaviouralResponse: YesNo?,

    val oshPolicyWorkplaceErgonomics: YesNo?,

    val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: YesNo?,

    val oshPolicyFireProtection: YesNo?,

    val oshPolicyWorkingHours: YesNo?,

    val oshPolicyTrainingAddressed: YesNo?,

    val oshPolicyTraining: YesNo?,

    val oshManagementSystem: YesNo?,

    val oshManagementSystemInternationalCertification: YesNo?,

    val oshManagementSystemNationalCertification: YesNo?,

    val workplaceAccidentsUnder10: YesNo?,

    val oshTraining: YesNo?,
)