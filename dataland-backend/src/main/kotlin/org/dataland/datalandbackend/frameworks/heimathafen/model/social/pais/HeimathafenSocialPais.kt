package org.dataland.datalandbackend.frameworks.heimathafen.model.social.pais

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Pais section
 */
data class HeimathafenSocialPais(
    val paiSozial: YesNo?,
    val verwendeteSchluesselzahlenFuerPaiSozial: String?,
    val datenerfassungFuerPaiSozial: String?,
    val datenPlausibilitaetspruefungFuerPaiSozial: String?,
    val datenquelleFuerPaiSozial: String?,
    val paiSozialesAufDemLand: YesNo?,
    val verwendeteSchluesselzahlenFuerPaiSozialesAufDemLand: String?,
    val datenerfassungFuerPaiSozialesAufDemLand: String?,
    val datenPlausibilitaetspruefungFuerPaiSozialesAufDemLand: String?,
    val datenquelleFuerPaiSozialesAufDemLand: String?,
)
