package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.FrequenzDerBerichterstattung
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.AnreizmechanismenFuerDasManagementUmwelt
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.AnreizmechanismenFuerDasManagementSoziales
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.StatusE
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.StatusS
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.StatusG
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint

/**
 * The data-model for the Allgemein section
 */
data class GdvAllgemein(
    val esgZiele: YesNo?,
    val ziele: String?,
    val investitionen: String?,
    val sektorMitHohenKlimaauswirkungen: YesNo?,
    val nachhaltigkeitsbericht: YesNo?,
    val frequenzDerBerichterstattung: FrequenzDerBerichterstattung?,
    val mechanismenZurUeberwachungDerEinhaltungUnGlobalCompactPrinzipienUndOderOecdLeitsaetze: YesNo?,
    val uncgPrinzipien: YesNo?,
    val erklaerungUngc: String?,
    val oecdLeitsaetze: YesNo?,
    val erklaerungOecd: String?,
    val ausrichtungAufDieUnSdgsUndAktivesVerfolgen: String?,
    val ausschlusslistenAufBasisVonEsgKriterien: YesNo?,
    val ausschlusslisten: String?,
    val oekologischeSozialeFuehrungsstandardsOderPrinzipien: YesNo?,
    val anreizmechanismenFuerDasManagementUmwelt: AnreizmechanismenFuerDasManagementUmwelt?,
    val anreizmechanismenFuerDasManagementSoziales: AnreizmechanismenFuerDasManagementSoziales?,
    val esgBezogeneRechtsstreitigkeiten: YesNo?,
    val rechtsstreitigkeitenMitBezugZuE: YesNo?,
    val statusE: StatusE?,
    val einzelheitenZuDenRechtsstreitigkeitenE: String?,
    val rechtsstreitigkeitenMitBezugZuS: YesNo?,
    val statusS: StatusS?,
    val einzelheitenZuDenRechtsstreitigkeitenS: String?,
    val rechtsstreitigkeitenMitBezugZuG: YesNo?,
    val statusG: StatusG?,
    val einzelheitenZuDenRechtsstreitigkeitenG: String?,
    val esgRating: YesNo?,
    val agentur: String?,
    val ergebnis: BaseDataPoint<String>?,
    val kritischePunkte: String?,
    val nachhaltigkeitsbezogenenAnleihen: YesNo?,
    val wichtigsteESUndGRisikenUndBewertung: String?,
    val hindernisseBeimUmgangMitEsgBedenken: String?,
)
