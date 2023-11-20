package org.dataland.datalandbackend.frameworks.heimathafen.model.general.implementierung

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import java.time.LocalDate
import java.math.BigDecimal

/**
 * The data-model for the Implementierung section
 */
data class HeimathafenGeneralImplementierung(
    val angeboteneSprachen: String?,
    val bereitgestellteDokumentationsarten: String?,
    val bereitgestellteDokumentationAufDeutsch: YesNo?,
    val leistungstests: YesNo?,
    val sicherheitstests: YesNo?,
    val beschreibungDerSystemarchitektur: String?,
    val erforderlichesClientBetriebssystem: String?,
    val angebotFuerFetteDuenneZitrischeKunden: YesNo?,
    val serverBackup: String?,
    val standardisiertesKonzeptZurWiederherstellungImKatastrophenfall: BaseDataPoint<YesNo>?,
    val stammUndBewegungsdatenLesen: YesNo?,
    val kompatibilitaetMitAnderenDatenquellen: YesNo?,
    val importDerErgebnisseInDasDataWarehouse: YesNo?,
    val erforderlichesDatenbanksystem: String?,
    val beschreibungDesDesignsUndDerStrukturDerDatenbankEn: String?,
    val direkterZugriffAufDieDatenbank: YesNo?,
    val schreibenderZugriffAufDieDatenbank: YesNo?,
    val unterstuetzungDerEchtzeitverarbeitung: YesNo?,
    val unterstuetzungFuerZeitnaheVerarbeitung: YesNo?,
    val unterstuetzungDerStapelverarbeitung: YesNo?,
    val unterstuetzteBiLoesung: String?,
    val flexibilitaetBeimImportExportVonDaten: YesNo?,
    val jederzeitVerfuegbar: YesNo?,
    val uebertragenVonDatenhistorien: String?,
    val unterstuetzterZeitraumDerDatenhistorien: String?,
    val fruehesterStartterminFuerEinIntegrationsprojekt: LocalDate?,
    val geschaetzterZeitrahmenFuerDieVollstaendigeIntegrationDesProjekts: String?,
    val durchschnittlicheAnzahlDerBenoetigtenRessourcen: BigDecimal?,
    val anzahlDerVerfuegbarenRessourcen: BigDecimal?,
    val kundenbetreuung: BigDecimal?,
)
