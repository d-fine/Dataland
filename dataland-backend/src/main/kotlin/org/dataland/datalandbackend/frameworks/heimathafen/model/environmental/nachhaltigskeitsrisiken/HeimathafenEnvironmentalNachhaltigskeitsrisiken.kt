// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.nachhaltigskeitsrisiken

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Nachhaltigskeitsrisiken section
 */
data class HeimathafenEnvironmentalNachhaltigskeitsrisiken(
    val methodikFuerOekologischeNachhaltigkeitsrisiken: YesNo? = null,
    val wennNeinBitteBegruenden: String? = null,
    val kartierteRisikenFuerDieOekologischeNachhaltigkeit: String? = null,
    val identifizierungDerWesentlichenRisikenFuerDieOekologischeNachhaltigkeitUndDerKonstruktionsmethodik: String? = null,
    val umweltbewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken: String? = null,
    val risikenFuerDieOekologischeNachhaltigkeitAbsichern: String? = null,
    @field:Valid()
    val quellen: List<BaseDataPoint<String>>? = null,
    val vierAugenPruefung: YesNo? = null,
    val wennKeineVierAugenPruefungBitteBegruenden: String? = null,
    val beschreibungDerVierAugenPruefung: String? = null,
)
