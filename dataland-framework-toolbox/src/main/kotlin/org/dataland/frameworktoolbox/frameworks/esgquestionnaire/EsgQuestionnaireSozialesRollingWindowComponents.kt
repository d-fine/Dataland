package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

/**
 * This object contains implementations for the rolling window components in the soziales category of the ESG
 * Questionnaire framework
 */
object EsgQuestionnaireSozialesRollingWindowComponents {

    /*
    fun auswirkungenAufAnteilBefristerVertraegeUndFluktuation(
        componentGroupSoziales: ComponentGroup,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("unternehmensstrukturaenderungen") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
            ) {
                decimalRows = mutableListOf(
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anzahlDerBefristetenVertraege",
                        "# der befristeten Verträge", "",
                    ),
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "fluktuation", "Fluktuation",
                        "%",
                    ),
                )
            }
        }
    } TODO */

    /*
    fun budgetFuerSchulungAusbildung(componentGroupSoziales: ComponentGroup) {
        componentGroupSoziales.edit<ComponentGroup>("sicherheitUndWeiterbildung") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "budgetFuerSchulungAusbildung",
            ) {
                decimalRows = mutableListOf(
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "unfallrate",
            ) {
                decimalRows = mutableListOf(
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "haeufigkeitsrateVonArbeitsunfaellenMitZeitverlust",
                        "Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust", "",
                    ),
                )
            }
        }
    }
 TODO */

    /*
    fun massnahmenZurVerbesserungDerEinkommensungleichheit(
        componentGroupSoziales: ComponentGroup,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("einkommensgleichheit") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "ueberwachungDerEinkommensungleichheit",
            ) {
                decimalRows = mutableListOf(
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "unbereinigtesGeschlechtsspezifischesLohngefaelle",
                        "Unbereinigtes geschlechtsspezifisches Lohngefälle", "%",
                    ),
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "einkommensungleichheitsverhaeltnis",
                        "Einkommensungleichheitsverhältnis", "%",
                    ),
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "ceoEinkommensungleichheitsverhaeltnis",
                        "CEO-Einkommensungleichheitsverhältnis", "%",
                    ),
                )
            }
        }
    } TODO*/
}
