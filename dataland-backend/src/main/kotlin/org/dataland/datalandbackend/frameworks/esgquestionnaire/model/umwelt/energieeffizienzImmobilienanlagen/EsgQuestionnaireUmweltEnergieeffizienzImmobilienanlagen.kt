// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.esgquestionnaire.model.umwelt.energieeffizienzImmobilienanlagen

import org.dataland.datalandbackend.frameworks.esgquestionnaire.custom.YearlyTimeseriesData

/**
 * The data-model for the EnergieeffizienzImmobilienanlagen section
 */
data class EsgQuestionnaireUmweltEnergieeffizienzImmobilienanlagen(
    val berichterstattungEnergieverbrauchVonImmobilienvermoegen:
        YearlyTimeseriesData<BerichterstattungEnergieverbrauchVonImmobilienvermoegenValues?>? = null,
    val unternehmensGruppenStrategieBzglEnergieeffizientenImmobilienanlagen: String? = null,
)
