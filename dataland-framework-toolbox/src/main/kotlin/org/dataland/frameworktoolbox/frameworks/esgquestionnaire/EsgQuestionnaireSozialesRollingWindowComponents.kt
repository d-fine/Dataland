package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import org.dataland.frameworktoolbox.frameworks.esgquestionnaire.custom
    .EsgQuestionnaireYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional

/**
 * This object contains implementations for the rolling window components in the soziales category of the GDV framework
 */
object EsgQuestionnaireSozialesRollingWindowComponents {
    /**
     * Creates the "Auswirkungen auf Anteil befrister Verträge und Fluktuation" field
     */
    fun auswirkungenAufAnteilBefristerVertraegeUndFluktuation(
        componentGroupSoziales: ComponentGroup,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("unternehmensstrukturaenderungen") {
            val vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur =
                get<YesNoComponent>("vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur")

            create<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
            ) {
                label = "Auswirkungen auf Anteil befrister Verträge und Fluktuation"
                uploadPageExplanation = "Bitte geben Sie die Anzahl der befristeten Verträge sowie die Fluktuation (%) für die" +
                    " letzten drei Jahre an."
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
                availableIf = DependsOnComponentValue(
                    vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur,
                    "Yes",
                )
                uploadBehaviour = EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearPast
            }
        }
    }

    /**
     * Creates the "Budget für Schulung/Ausbildung" field
     */
    fun budgetFuerSchulungAusbildung(componentGroupSoziales: ComponentGroup, available: FrameworkConditional) {
        componentGroupSoziales.edit<ComponentGroup>("sicherheitUndWeiterbildung") {
            create<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "budgetFuerSchulungAusbildung",
            ) {
                label = "Budget für Schulung/Ausbildung"
                uploadPageExplanation = "Bitte geben Sie an wie hoch das Budget ist, das pro Mitarbeiter und Jahr für " +
                    "Schulungen/Fortbildungen in den letzten drei Jahren ausgegeben wurde."
                decimalRows = mutableListOf(
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "budgetProMitarbeiter",
                        "Budget pro Mitarbeiter", "€",
                    ),
                )
                availableIf = available
                uploadBehaviour = EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearPast
            }
        }
    }

    /**
     * Creates the "Unfallrate" field
     */
    fun unfallrate(componentGroupSoziales: ComponentGroup, available: FrameworkConditional) {
        componentGroupSoziales.edit<ComponentGroup>("sicherheitUndWeiterbildung") {
            create<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "unfallrate",
                "budgetFuerSchulungAusbildung",
            ) {
                label = "Unfallrate"
                uploadPageExplanation = "Wie hoch war die Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust für die letzten " +
                    "drei Jahre?"
                decimalRows = mutableListOf(
                    EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "haeufigkeitsrateVonArbeitsunfaellenMitZeitverlust",
                        "Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust", "",
                    ),
                )
                availableIf = available
                uploadBehaviour = EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearPast
            }
        }
    }

    /**
     * Creates the "Überwachung der Einkommensungleichheit" field
     */
    fun massnahmenZurVerbesserungDerEinkommensungleichheit(
        componentGroupSoziales: ComponentGroup,
        available: FrameworkConditional,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("einkommensgleichheit") {
            create<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "ueberwachungDerEinkommensungleichheit",
                "massnahmenZurVerbesserungDerEinkommensungleichheit",
            ) {
                label = "Überwachung der Einkommensungleichheit"
                uploadPageExplanation = "Bitte geben Sie das unbereinigte geschlechtsspezifische Lohngefälle, das " +
                    "Einkommensungleichheitsverhältnis, sowie das CEO-Einkommensungleichheitsverhältnis für" +
                    " die letzten drei Jahre an."
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
                availableIf = available
                uploadBehaviour = EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearPast
            }
        }
    }
}
