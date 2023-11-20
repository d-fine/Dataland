package org.dataland.datalandbackend.frameworks.heimathafen.model.social.kontroverseGeschaeftsfelder

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the KontroverseGeschaeftsfelder section
 */
data class HeimathafenSocialKontroverseGeschaeftsfelder(
    val herstellungOderVertriebVonWaffenAusschluss: YesNo?,
    val wennHerstellungOderVertriebVonWaffenAusschlussNeinBitteBegruenden: String?,
    val metrischVerwendetFuerHerstellungOderVertriebVonWaffenAusschluss: String?,
    val methodikDerBerechnungFuerHerstellungOderVertriebVonWaffenAusschluss: String?,
    val verwendeteQuellenFuerHerstellungOderVertriebVonWaffenAusschluss: String?,
    val ausschlussVerbotenerWaffen: YesNo?,
    val wennAusschlussVerbotenerWaffenNeinBitteBegruenden: String?,
    val metrischVerwendetFuerAusschlussVerbotenerWaffen: String?,
    val verwendeteQuellenFuerAusschlussVerbotenerWaffen: String?,
)
