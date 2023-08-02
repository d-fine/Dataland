package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "General violations" belonging to the category "Governance" of the lksg framework.
*/
data class LksgGovernanceGeneralViolations(
      val responsibilitiesForFairWorkingConditions: YesNo? = null,

      val responsibilitiesForTheEnvironment: YesNo? = null,

      val responsibilitiesForOccupationalSafety: YesNo? = null,

      val legalProceedings: YesNo? = null,

      val humanRightsViolationS: YesNo? = null,

      val humanRightsViolations: String? = null,

      val humanRightsViolationAction: YesNo? = null,

      val humanRightsViolationActionMeasures: String? = null,

      val highRiskCountriesRawMaterials: YesNo? = null,

      val highRiskCountriesRawMaterialsLocation: List<String>? = null,

      val highRiskCountriesActivity: YesNo? = null,

      val highRiskCountries: List<String>? = null,

      val highRiskCountriesProcurement: YesNo? = null,

      val highRiskCountriesProcurementName: List<String>? = null,
)
