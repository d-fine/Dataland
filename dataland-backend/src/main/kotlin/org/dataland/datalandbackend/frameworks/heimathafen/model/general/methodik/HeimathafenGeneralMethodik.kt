package org.dataland.datalandbackend.frameworks.heimathafen.model.general.methodik

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Methodik section
 */
data class HeimathafenGeneralMethodik(
    val verstaendnisVonNachhaltigkeitAlsTeilDerBewertung: String?,
    val kriterienFuerIhreNachhaltigkeitsratings: String?,
    val verfahrenZurVorbereitungDerAnalyseOderMethodik: String?,
    val wieIstIhreBewertungsskalaDefiniert: String?,
    val bewertungAktuell: String?,
    val sindIhreBewertungenUnabhaengig: String?,
    val datenerfassung: String?,
    val dieMethodikUmfasstUmweltSozialesUndGovernance: YesNo?,
    val datenquelle: String?,
    val datenPlausibilitaetspruefung: String?,
    val intervalleFuerDieDatenaktualisierung: String?,
    val zuverlaessigkeitDerMethodikSicherstellen: String?,
    val minimierenOderVerhindernSieSubjektiveFaktoren: String?,
    val listePotenziellerInteressenkonflikte: String?,
    val interessenkonfliktenEntgegenwirken: String?,
    val dokumentationDerDatenerfassungUndSicherstellungDesProzesses: String?,
    val bewertungVonQualitaetsstandards: String?,
    val ratingTransparenzstandards: String?,
    val qualitaetssicherungsprozess: YesNo?,
    val fallsNeinGebenSieBitteDieGruendeAn: String?,
    val strukturDesQualitaetssicherungsprozesses: String?,
    val dieAktualitaetDerMethodik: String?,
    val paisInDieAnalyseEinbezogen: YesNo?,
    val listeDerEingeschlossenenPais: String?,
    val quelleDerPaiSammlung: String?,
    val umgangMitAusreissern: String?,
    val identifizierungVonKontroversenGeschaeften: String?,
    val aktuelleKontroversen: String?,
    val kontroversenUmDieQuellenerfassung: String?,
)
