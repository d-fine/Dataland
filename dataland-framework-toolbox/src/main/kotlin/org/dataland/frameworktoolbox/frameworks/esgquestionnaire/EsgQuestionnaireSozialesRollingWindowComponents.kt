package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import org.dataland.frameworktoolbox.frameworks.esgquestionnaire.custom
    .EsgQuestionnaireYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit

/**
 * This object contains implementations for the rolling window components in the soziales category of the ESG
 * Questionnaire framework
 */
object EsgQuestionnaireSozialesRollingWindowComponents {
    /**
     * Creates the "Auswirkungen auf Anteil befrister Verträge und Fluktuation" field
     */
    fun auswirkungenAufAnteilBefristerVertraegeUndFluktuation(componentGroupSoziales: ComponentGroup) {
        componentGroupSoziales.edit<ComponentGroup>("unternehmensstrukturaenderungen") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
            ) {
                decimalRows =
                    mutableListOf(
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
    }

    /**
     * Creates the "Budget für Schulung/Ausbildung" field
     */
    fun budgetFuerSchulungAusbildung(componentGroupSoziales: ComponentGroup) {
        componentGroupSoziales.edit<ComponentGroup>("sicherheitUndWeiterbildung") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "budgetFuerSchulungAusbildung",
            ) {
                decimalRows =
                    mutableListOf(
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
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "haeufigkeitsrateVonArbeitsunfaellenMitZeitverlust",
                            "Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust", "",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Überwachung der Einkommensungleichheit" field
     */
    fun massnahmenZurVerbesserungDerEinkommensungleichheit(componentGroupSoziales: ComponentGroup) {
        componentGroupSoziales.edit<ComponentGroup>("einkommensgleichheit") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "ueberwachungDerEinkommensungleichheit",
            ) {
                decimalRows =
                    mutableListOf(
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
    }
}
