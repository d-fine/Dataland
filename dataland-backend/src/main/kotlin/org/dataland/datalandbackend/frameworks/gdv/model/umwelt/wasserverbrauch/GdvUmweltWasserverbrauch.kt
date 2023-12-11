package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.wasserverbrauch

import org.dataland.datalandbackend.model.gdv.YearlyTimeseriesData

/**
 * The data-model for the Wasserverbrauch section
 */
data class GdvUmweltWasserverbrauch(
    val unternehmensGruppenStrategieBzglWasserverbrauch: String?,
    val berichterstattungWasserverbrauch: YearlyTimeseriesData<BerichterstattungWasserverbrauchValues?>?,
)
