package org.dataland.datalandbackend.frameworks.heimathafen.model.social.nachhaltigskeitsrisiken

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Nachhaltigskeitsrisiken section
 */
data class HeimathafenSocialNachhaltigskeitsrisiken(
    val methodikSozialeNachhaltigkeitsrisiken: YesNo?,
    val wennMethodikSozialeNachhaltigkeitsrisikenNeinBitteBegruenden: String?,
    val kartierteSozialeNachhaltigkeitsrisiken: String?,
    val identifizierungWesentlicherSozialerNachhaltigkeitsrisikenUndKonstruktionsmethodik: String?,
    val sozialeBewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken: String?,
    val sozialeNachhaltigkeitsrisikenAbsichern: String?,
    val quelle: String?,
    val vierAugenPruefung: YesNo?,
    val wennVierAugenPruefungNeinBitteBegruenden: String?,
    val beschreibungDerVierAugenPruefung: String?,
)
