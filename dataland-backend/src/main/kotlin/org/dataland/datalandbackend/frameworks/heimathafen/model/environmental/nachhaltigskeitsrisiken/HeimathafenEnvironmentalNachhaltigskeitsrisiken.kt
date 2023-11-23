package org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.nachhaltigskeitsrisiken

import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Nachhaltigskeitsrisiken section
 */
data class HeimathafenEnvironmentalNachhaltigskeitsrisiken(
    val methodikFuerOekologischeNachhaltigkeitsrisiken: YesNo?,
    val wennMethodikFuerOekologischeNachhaltigkeitsrisikenNeinBitteBegruenden: String?,
    val kartierteRisikenFuerDieOekologischeNachhaltigkeit: String?,
    val identifizierungDerWesentlichenRisikenFuerDieOekologischeNachhaltigkeitUndDerKonstruktionsmethodik: String?,
    val umweltbewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken: String?,
    val risikenFuerDieOekologischeNachhaltigkeitAbsichern: String?,
    val quelle: BaseDataPoint<String>?,
    val vierAugenPruefung: YesNo?,
    val wennVierAugenPruefungNeinBitteBegruenden: String?,
    val beschreibungDerVierAugenPruefung: String?,
)
