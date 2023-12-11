package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.energieeffizienzImmobilienanlagen

import org.dataland.datalandbackend.model.gdv.YearlyTimeseriesData

/**
 * The data-model for the EnergieeffizienzImmobilienanlagen section
 */
data class GdvUmweltEnergieeffizienzImmobilienanlagen(
    val unternehmensGruppenStrategieBzglEnergieeffizientenImmobilienanlagen: String?,
    val berichterstattungEnergieverbrauchVonImmobilienvermoegen: YearlyTimeseriesData<BerichterstattungEnergieverbrauchVonImmobilienvermoegenValues?>?,
)
