package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.treibhausgasemissionen

import org.dataland.datalandbackend.model.gdv.YearlyTimeseriesData

/**
 * The data-model for the Treibhausgasemissionen section
 */
data class GdvUmweltTreibhausgasemissionen(
    val treibhausgasEmissionsintensitaetDerUnternehmenInDieInvestriertWird: String?,
    val strategieUndZieleZurReduzierungVonTreibhausgasEmissionen: String?,
    val treibhausgasBerichterstattungUndPrognosen: YearlyTimeseriesData<TreibhausgasBerichterstattungUndPrognosenValues?>?,
)
