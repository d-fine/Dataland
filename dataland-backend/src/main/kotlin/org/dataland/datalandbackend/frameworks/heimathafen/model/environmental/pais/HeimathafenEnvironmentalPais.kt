package org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.pais

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Pais section
 */
data class HeimathafenEnvironmentalPais(
    val sechsPaisTreibhausgasemissionen: YesNo?,
    val wennSechsPaisTreibhausgasemissionenNeinBitteBegruenden: String?,
    val wennJaBitteDiePaisAuflisten: String?,
    val verwendeteSchluesselzahlenFuerSechsPaisTreibhausgasemissionen: String?,
    val datenerfassungFuerSechsPaisTreibhausgasemissionen: String?,
    val datenPlausibilitaetspruefungFuerSechsPaisTreibhausgasemissionen: String?,
    val datenquelleFuerSechsPaisTreibhausgasemissionen: String?,
    val paisBiologischeVielfalt: YesNo?,
    val wennPaisBiologischeVielfaltNeinBitteBegruenden: String?,
    val verwendeteSchluesselzahlenFuerPaisBiologischeVielfalt: String?,
    val datenerfassungFuerPaisBiologischeVielfalt: String?,
    val datenPlausibilitaetspruefungFuerPaisBiologischeVielfalt: String?,
    val datenquelleFuerPaisBiologischeVielfalt: String?,
    val paiWasser: YesNo?,
    val wennPaiWasserNeinBitteBegruenden: String?,
    val verwendeteSchluesselzahlenFuerPaiWasser: String?,
    val datenerfassungFuerPaiWasser: String?,
    val datenPlausibilitaetspruefungFuerPaiWasser: String?,
    val datenquelleFuerPaiWasser: String?,
    val paiAbfall: YesNo?,
    val wennPaiAbfallNeinBitteBegruenden: String?,
    val verwendeteSchluesselzahlenFuerPaiAbfall: String?,
    val datenerfassungFuerPaiAbfall: String?,
    val datenPlausibilitaetspruefungFuerPaiAbfall: String?,
    val datenquelleFuerPaiAbfall: String?,
    val paiUmweltAufDemLand: YesNo?,
    val wennPaiUmweltAufDemLandNeinBitteBegruenden: String?,
    val verwendeteSchluesselzahlenFuerPaiUmweltAufDemLand: String?,
    val datenerfassungFuerPaiUmweltAufDemLand: String?,
    val datenPlausibilitaetspruefungFuerPaiUmweltAufDemLand: String?,
    val datenquelleFuerPaiUmweltAufDemLand: String?,
)
