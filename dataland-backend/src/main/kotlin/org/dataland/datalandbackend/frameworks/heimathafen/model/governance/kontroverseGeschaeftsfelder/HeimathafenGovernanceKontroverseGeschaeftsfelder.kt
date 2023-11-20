package org.dataland.datalandbackend.frameworks.heimathafen.model.governance.kontroverseGeschaeftsfelder

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the KontroverseGeschaeftsfelder section
 */
data class HeimathafenGovernanceKontroverseGeschaeftsfelder(
    val kontroversenImBereichDerBestechungUndKorruption: YesNo?,
    val wennKontroversenImBereichDerBestechungUndKorruptionNeinBitteBegruenden: String?,
    val verwendeteMetrikenUndMethodik: String?,
    val verwendeteQuellenFuerKontroversenImBereichDerBestechungUndKorruption: String?,
    val dieAktualitaetDerKontroversenImBereichBestechungUndKorruption: String?,
)
