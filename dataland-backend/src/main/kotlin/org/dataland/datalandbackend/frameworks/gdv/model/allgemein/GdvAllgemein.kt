package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.FrequenzDerBerichterstattungOptions
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.AnreizmechanismenFuerDasManagementUmweltOptions
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.AnreizmechanismenFuerDasManagementSozialesOptions
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.StatusEOptions
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.StatusSOptions
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.StatusGOptions
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
    val frequenzDerBerichterstattung: FrequenzDerBerichterstattungOptions?,
    val mechanismenZurUeberwachungDerEinhaltungUnGlobalCompactPrinzipienUndOderOecdLeitsaetze: YesNo?,
    val uncgPrinzipien: YesNo?,
    val erklaerungUngc: String?,
    val oecdLeitsaetze: YesNo?,
    val erklaerungOecd: String?,
    val ausrichtungAufDieUnSdgsUndAktivesVerfolgen: String?,
    val ausschlusslistenAufBasisVonEsgKriterien: YesNo?,
    val ausschlusslisten: String?,
    val oekologischeSozialeFuehrungsstandardsOderPrinzipien: YesNo?,
    val anreizmechanismenFuerDasManagementUmwelt: AnreizmechanismenFuerDasManagementUmweltOptions?,
    val anreizmechanismenFuerDasManagementSoziales: AnreizmechanismenFuerDasManagementSozialesOptions?,
    val esgBezogeneRechtsstreitigkeiten: YesNo?,
    val rechtsstreitigkeitenMitBezugZuE: YesNo?,
    val statusE: StatusEOptions?,
    val einzelheitenZuDenRechtsstreitigkeitenE: String?,
    val rechtsstreitigkeitenMitBezugZuS: YesNo?,
    val statusS: StatusSOptions?,
    val einzelheitenZuDenRechtsstreitigkeitenS: String?,
    val rechtsstreitigkeitenMitBezugZuG: YesNo?,
    val statusG: StatusGOptions?,
    val einzelheitenZuDenRechtsstreitigkeitenG: String?,
    val esgRating: YesNo?,
    val agentur: String?,
    val ergebnis: BaseDataPoint<String>?,
    val kritischePunkte: String?,
    val nachhaltigkeitsbezogenenAnleihen: YesNo?,
    val wichtigsteESUndGRisikenUndBewertung: String?,
    val hindernisseBeimUmgangMitEsgBedenken: String?,
)
