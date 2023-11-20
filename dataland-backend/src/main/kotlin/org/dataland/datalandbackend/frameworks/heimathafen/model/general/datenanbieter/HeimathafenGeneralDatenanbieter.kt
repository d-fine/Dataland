package org.dataland.datalandbackend.frameworks.heimathafen.model.general.datenanbieter

import java.math.BigDecimal

/**
 * The data-model for the Datenanbieter section
 */
data class HeimathafenGeneralDatenanbieter(
    val unternehmenseigentumUndEigentuemerstruktur: String?,
    val kernkompetenzenUndGeschaeftsbereiche: String?,
    val anzahlDerFuerEsgZustaendigenMitarbeiter: BigDecimal?,
)
