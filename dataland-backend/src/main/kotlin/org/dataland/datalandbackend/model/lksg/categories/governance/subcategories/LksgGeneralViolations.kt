package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "General violations"
 */
data class LksgGeneralViolations(

    val responsibilitiesForFairWorkingConditions: YesNo?,

    val responsibilitiesForTheEnvironment: YesNo?,

    val responsibilitiesForOccupationalSafety: YesNo?,

    val legalProceedings: YesNo?,

    val humanRightsViolation: YesNo?,

    val humanRightsViolations: String?,

    val humanRightsViolationAction: YesNo?,

    val humanRightsViolationActionMeasures: String?,

    val highRiskCountriesRawMaterials: YesNo?,

    val highRiskCountriesRawMaterialsLocation: String?,

    val highRiskCountriesActivity: YesNo?,

    val highRiskCountries: String?,

    val highRiskCountriesProcurement: YesNo?,

    val highRiskCountriesProcurementName: String?,
)
