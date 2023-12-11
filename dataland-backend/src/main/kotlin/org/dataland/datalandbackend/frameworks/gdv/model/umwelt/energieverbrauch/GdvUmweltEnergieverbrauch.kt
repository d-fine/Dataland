package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.energieverbrauch

import org.dataland.datalandbackend.model.gdv.YearlyTimeseriesData

/**
 * The data-model for the Energieverbrauch section
 */
data class GdvUmweltEnergieverbrauch(
    val unternehmensGruppenStrategieBzglEnergieverbrauch: String?,
    val berichterstattungEnergieverbrauch: YearlyTimeseriesData<BerichterstattungEnergieverbrauchValues?>?,
)
