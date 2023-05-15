package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "General violations"
 */
data class LksgGeneralViolations(

    val responsibilitiesForFairWorkingConditions: BaseDataPoint<YesNo>?,

    val responsibilitiesForTheEnvironment: BaseDataPoint<YesNo>?,

    val responsibilitiesForOccupationalSafety: BaseDataPoint<YesNo>?,

    val legalProceedings: BaseDataPoint<YesNo>?,

    val humanRightsViolation: BaseDataPoint<YesNo>?,

    val humanRightsViolations: BaseDataPoint<String>?,

    val humanRightsViolationAction: BaseDataPoint<YesNo>?,

    val humanRightsViolationActionMeasures: BaseDataPoint<String>?,

    val highRiskCountriesRawMaterials: BaseDataPoint<YesNo>?,

    val highRiskCountriesRawMaterialsLocation: BaseDataPoint<List<String>>?,

    val highRiskCountriesActivity: BaseDataPoint<YesNo>?,

    val highRiskCountries: BaseDataPoint<List<String>>?,

    val highRiskCountriesProcurement: BaseDataPoint<YesNo>?,

    val highRiskCountriesProcurementName: BaseDataPoint<List<String>>?,
)
