package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Disregard for occupational health / safety"
 */
data class LksgDisregardForOccupationalHealthSafety(
    val lowSkillWork: YesNo?,

    val hazardousMachines: YesNo?,

    val oshPolicy: YesNo?,

    val oshPolicyPersonalProtectiveEquipment: YesNoNa?,

    val oshPolicyMachineSafety: YesNoNa?,

    val oshPolicyDisasterBehaviouralResponse: YesNo?,

    val oshPolicyAccidentsBehaviouralResponse: YesNo?,

    val oshPolicyWorkplaceErgonomics: YesNo?,

    val oshPolicyAccessToWork: YesNo?,

    val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: YesNoNa?,

    val oshPolicyFireProtection: YesNo?,

    val oshPolicyWorkingHours: YesNo?,

    val oshPolicyTrainingAddressed: YesNo?,

    val oshPolicyTraining: YesNo?,

    val oshManagementSystem: YesNo?,

    val oshManagementSystemInternationalCertification: YesNo?,

    val oshManagementSystemNationalCertification: YesNo?,

    val workplaceAccidentsUnder10: YesNo?,

    val oshTraining: YesNo?,

    val healthAndSafetyPolicy: YesNo?,
)
