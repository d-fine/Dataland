package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Allgemein section
 */
data class GdvAllgemein(
    val esgZiele: YesNo?,
    val ziele: String?,
    val investitionen: String?,
    val sektorMitHohenKlimaauswirkungen: YesNo?,
    val sektor: List<String>?,
    val nachhaltigkeitsbericht: YesNo?,
    val frequenzDerBerichterstattung: FrequenzDerBerichterstattungOptions?,
    val iso14001: BaseDataPoint<YesNo>?,
    val iso45001: BaseDataPoint<YesNo>?,
    val iso27001: BaseDataPoint<YesNo>?,
    val iso50001: BaseDataPoint<YesNo>?,
    val weitereAkkreditierungen: List<BaseDataPoint<String>>?,
    val mechanismenZurUeberwachungDerEinhaltungUnGlobalCompactPrinzipienUndOderOecdLeitsaetze: YesNo?,
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
    val statusZuE: StatusZuEOptions?,
    val einzelheitenZuDenRechtsstreitigkeitenZuE: String?,
    val rechtsstreitigkeitenMitBezugZuS: YesNo?,
    val statusZuS: StatusZuSOptions?,
    val einzelheitenZuDenRechtsstreitigkeitenZuS: String?,
    val rechtsstreitigkeitenMitBezugZuG: YesNo?,
    val statusZuG: StatusZuGOptions?,
    val einzelheitenZuDenRechtsstreitigkeitenZuG: String?,
    val esgRating: YesNo?,
    val agentur: String?,
    val ergebnis: BaseDataPoint<String>?,
    val kritischePunkte: String?,
    val nachhaltigkeitsbezogenenAnleihen: YesNo?,
    val wichtigsteESUndGRisikenUndBewertung: String?,
    val hindernisseBeimUmgangMitEsgBedenken: String?,
)
