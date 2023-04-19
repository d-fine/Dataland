package org.dataland.datalandbackend.model.lksg.categories.governance.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Human rights"
 */
data class LksgHumanRights(
    @field:JsonProperty(required = true)
    val codeOfConduct: YesNo,

    @field:JsonProperty(required = true)
    val codeOfConductTraining: YesNo,

    @field:JsonProperty(required = true)
    val supplierCodeOfConduct: YesNo,

    @field:JsonProperty(required = true)
    val policyStatement: YesNo,

    val humanRightsStrategy: List<String>?,

    @field:JsonProperty(required = true)
    val environmentalImpactPolicy: YesNo,

    @field:JsonProperty(required = true)
    val fairWorkingConditionsPolicy: YesNo,

    @field:JsonProperty(required = true)
    val responsibilitiesForFairWorkingConditions: YesNo,

    @field:JsonProperty(required = true)
    val responsibilitiesForTheEnvironment: YesNo,

    @field:JsonProperty(required = true)
    val responsibilitiesForOccupationalSafety: YesNo,

    @field:JsonProperty(required = true)
    val legalProceedings: YesNo,

    @field:JsonProperty(required = true)
    val humanRightsViolation: YesNo,

    @field:JsonProperty(required = true)
    val humanRightsViolationLocation: List<String>,

    @field:JsonProperty(required = true)
    val humanRightsViolationAction: YesNo,

    @field:JsonProperty(required = true)
    val humanRightsViolationActionMeasures: List<String>,

    @field:JsonProperty(required = true)
    val highRiskCountriesRawMaterials: YesNo,

    @field:JsonProperty(required = true)
    val highRiskCountriesRawMaterialsLocation: List<String>,

    @field:JsonProperty(required = true)
    val highRiskCountriesActivity: YesNo,

    @field:JsonProperty(required = true)
    val highRiskCountries: List<String>,

    @field:JsonProperty(required = true)
    val highRiskCountriesProcurement: YesNo,

    val highRiskCountriesProcurementName: List<String>?,
)
