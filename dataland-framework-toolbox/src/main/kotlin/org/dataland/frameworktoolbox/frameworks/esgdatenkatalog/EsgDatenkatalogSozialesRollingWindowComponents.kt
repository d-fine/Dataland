package org.dataland.frameworktoolbox.frameworks.esgdatenkatalog

/**
 * This object contains implementations for the rolling window components in the soziales category of the ESG
 * Datenkatalog framework
 */
object EsgDatenkatalogSozialesRollingWindowComponents {
    /*
    fun auswirkungenAufAnteilBefristerVertraegeUndFluktuation(
        componentGroupSoziales: ComponentGroup,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("unternehmensstrukturaenderungen") {
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
            ) {
                decimalRows = mutableListOf(
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anzahlDerBefristetenVertraege",
                        "# der befristeten Verträge", "",
                    ),
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "fluktuation", "Fluktuation",
                        "%",
                    ),
                )
            }
        }
    }


    fun budgetFuerSchulungAusbildung(componentGroupSoziales: ComponentGroup) {
        componentGroupSoziales.edit<ComponentGroup>("sicherheitUndWeiterbildung") {
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "budgetFuerSchulungAusbildung",
            ) {
                decimalRows = mutableListOf(
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "budgetProMitarbeiter",
                        "Budget pro Mitarbeiter", "€",
                    ),
                )
            }
        }
    }

    /**
     * Creates the "Unfallrate" field
     */
    fun unfallrate(componentGroupSoziales: ComponentGroup) {
        componentGroupSoziales.edit<ComponentGroup>("sicherheitUndWeiterbildung") {
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "unfallrate",
            ) {
                decimalRows = mutableListOf(
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "haeufigkeitsrateVonArbeitsunfaellenMitZeitverlust",
                        "Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust", "",
                    ),
                )
            }
        }
    }



    fun massnahmenZurVerbesserungDerEinkommensungleichheit(
        componentGroupSoziales: ComponentGroup,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("einkommensgleichheit") {
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "ueberwachungDerEinkommensungleichheit",
            ) {
                decimalRows = mutableListOf(
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "unbereinigtesGeschlechtsspezifischesLohngefaelle",
                        "Unbereinigtes geschlechtsspezifisches Lohngefälle", "%",
                    ),
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "einkommensungleichheitsverhaeltnis",
                        "Einkommensungleichheitsverhältnis", "%",
                    ),
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "ceoEinkommensungleichheitsverhaeltnis",
                        "CEO-Einkommensungleichheitsverhältnis", "%",
                    ),
                )
            }
        }
    } TODO*/
}
