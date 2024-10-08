// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.heimathafen.model.general.implementierung

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigInteger
import java.time.LocalDate

/**
 * The data-model for the Implementierung section
 */
data class HeimathafenGeneralImplementierung(
    val angeboteneSprachen: String? = null,
    val bereitgestellteDokumentationsarten: String? = null,
    val bereitgestellteDokumentationAufDeutsch: YesNo? = null,
    val leistungstests: YesNo? = null,
    val sicherheitstests: YesNo? = null,
    val beschreibungDerSystemarchitektur: String? = null,
    val erforderlichesClientBetriebssystem: String? = null,
    val angebotFuerFetteDuenneZitrischeKunden: YesNo? = null,
    val serverBackup: String? = null,
    @field:Valid()
    val standardisiertesKonzeptZurWiederherstellungImKatastrophenfall: BaseDataPoint<YesNo>? = null,
    val stammUndBewegungsdatenLesen: YesNo? = null,
    val kompatibilitaetMitAnderenDatenquellen: YesNo? = null,
    val importDerErgebnisseInDasDataWarehouse: YesNo? = null,
    val erforderlichesDatenbanksystem: String? = null,
    val beschreibungDesDesignsUndDerStrukturDerDatenbankEn: String? = null,
    val direkterZugriffAufDieDatenbank: YesNo? = null,
    val schreibenderZugriffAufDieDatenbank: YesNo? = null,
    val unterstuetzungDerEchtzeitverarbeitung: YesNo? = null,
    val unterstuetzungFuerZeitnaheVerarbeitung: YesNo? = null,
    val unterstuetzungDerStapelverarbeitung: YesNo? = null,
    val unterstuetzteBiLoesung: String? = null,
    val flexibilitaetBeimImportExportVonDaten: YesNo? = null,
    val rundUmDieUhrVerfuegbarkeit: YesNo? = null,
    val uebertragenVonDatenhistorien: String? = null,
    val unterstuetzterZeitraumDerDatenhistorien: String? = null,
    val fruehesterStartterminFuerEinIntegrationsprojekt: LocalDate? = null,
    val geschaetzterZeitrahmenFuerDieVollstaendigeIntegrationDesProjekts: String? = null,
    val durchschnittlicheAnzahlDerBenoetigtenRessourcen: BigInteger? = null,
    val anzahlDerVerfuegbarenRessourcen: String? = null,
    val kundenbetreuung: BigInteger? = null,
)
