package org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.kontroverseGeschaeftsfelder

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the KontroverseGeschaeftsfelder section
 */
data class HeimathafenEnvironmentalKontroverseGeschaeftsfelder(
    val ausschlussDerTabakerzeugung: YesNo?,
    val wennAusschlussDerTabakerzeugungNeinBitteBegruenden: String?,
    val metrischVerwendetFuerAusschlussDerTabakerzeugung: String?,
    val methodikDerBerechnungFuerAusschlussDerTabakerzeugung: String?,
    val verwendeteQuellenFuerAusschlussDerTabakerzeugung: String?,
    val ausschlussDerKohlefoerderungUndVerteilung: YesNo?,
    val wennAusschlussDerKohlefoerderungUndVerteilungNeinBitteBegruenden: String?,
    val metrischVerwendetFuerAusschlussDerKohlefoerderungUndVerteilung: String?,
    val methodikDerBerechnungFuerAusschlussDerKohlefoerderungUndVerteilung: String?,
    val verwendeteQuellenFuerAusschlussDerKohlefoerderungUndVerteilung: String?,
)
