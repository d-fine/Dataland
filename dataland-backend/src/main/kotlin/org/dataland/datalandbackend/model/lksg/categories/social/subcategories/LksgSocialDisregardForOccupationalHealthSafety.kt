package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the subcategory "Disregard for occupational health/safety" belonging to the category "Social" of the
 * Lksg framework.
*/
data class LksgSocialDisregardForOccupationalHealthSafety(
    val lowSkillWork: YesNo? = null,

    val hazardousMachines: YesNo? = null,

    val oshPolicy: YesNo? = null,

    val oshPolicyPersonalProtectiveEquipment: YesNoNa? = null,

    val oshPolicyMachineSafety: YesNoNa? = null,

    val oshPolicyDisasterBehavioralResponse: YesNo? = null,

    val oshPolicyAccidentsBehavioralResponse: YesNo? = null,

    val oshPolicyWorkplaceErgonomics: YesNo? = null,

    val oshPolicyAccessToWork: YesNo? = null,

    val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: YesNoNa? = null,

    val oshPolicyFireProtection: YesNo? = null,

    val oshPolicyWorkingHours: YesNo? = null,

    val oshPolicyTrainingAddressed: YesNo? = null,

    val oshPolicyTraining: YesNo? = null,

    val oshManagementSystem: YesNo? = null,

    val oshManagementSystemInternationalCertification: BaseDataPoint<YesNo>? = null,

    val oshManagementSystemNationalCertification: BaseDataPoint<YesNo>? = null,

    val under10WorkplaceAccidents: YesNo? = null,

    val oshTraining: YesNo? = null,

    val healthAndSafetyPolicy: BaseDataPoint<YesNo>? = null,
)
