package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Disregard for occupational health / safety"
 */
data class LksgDisregardForOccupationalHealthSafety(
        val lowSkillWork: BaseDataPoint<YesNo>?,

        val hazardousMachines: BaseDataPoint<YesNo>?,

        val oshPolicy: BaseDataPoint<YesNo>?,

        val oshPolicyPersonalProtectiveEquipment: BaseDataPoint<YesNoNa>?,

        val oshPolicyMachineSafety: BaseDataPoint<YesNoNa>?,

        val oshPolicyDisasterBehaviouralResponse: BaseDataPoint<YesNo>?,

        val oshPolicyAccidentsBehaviouralResponse: BaseDataPoint<YesNo>?,

        val oshPolicyWorkplaceErgonomics: BaseDataPoint<YesNo>?,

        val oshPolicyAccessToWork: BaseDataPoint<YesNo>?,

        val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: BaseDataPoint<YesNoNa>?,

        val oshPolicyFireProtection: BaseDataPoint<YesNo>?,

        val oshPolicyWorkingHours: BaseDataPoint<YesNo>?,

        val oshPolicyTrainingAddressed: BaseDataPoint<YesNo>?,

        val oshPolicyTraining: BaseDataPoint<YesNo>?,

        val oshManagementSystem: BaseDataPoint<YesNo>?,

        val oshManagementSystemInternationalCertification: BaseDataPoint<YesNo>?,

        val oshManagementSystemNationalCertification: BaseDataPoint<YesNo>?,

        val workplaceAccidentsUnder10: BaseDataPoint<YesNo>?,

        val oshTraining: BaseDataPoint<YesNo>?,

        val healthAndSafetyPolicy: BaseDataPoint<YesNo>?,
)
