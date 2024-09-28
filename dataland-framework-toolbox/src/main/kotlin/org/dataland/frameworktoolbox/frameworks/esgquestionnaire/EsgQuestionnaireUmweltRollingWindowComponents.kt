package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import org.dataland.frameworktoolbox.frameworks.esgquestionnaire.custom
    .EsgQuestionnaireYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit

/**
 * This object contains implementations for the rolling window components in the umwelt category of the ESG
 * Questionnaire framework
 */
object EsgQuestionnaireUmweltRollingWindowComponents {
    /**
     * Create the "Treibhausgas-Berichterstattung und Prognosen" field
     */
    fun treibhausgasBerichterstattungUndPrognosen(componentGroupUmwelt: ComponentGroup) {
        val tCo2UnitString = "tCO2-Äquiv."
        componentGroupUmwelt.edit<ComponentGroup>("treibhausgasemissionen") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "treibhausgasBerichterstattungUndPrognosen",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "scope1", "Scope 1",
                            tCo2UnitString,
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "scope2", "Scope 2",
                            tCo2UnitString,
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "scope3", "Scope 3",
                            tCo2UnitString,
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Berichterstattung Energieverbrauch" field
     */
    fun berichterstattungEnergieverbrauch(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("energieverbrauch") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauch",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "energieverbrauch",
                            "Energieverbrauch", "GWh",
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentDesVerbrauchsErneuerbarerEnergien",
                            "% des Verbrauchs erneuerbarer Energien", "%",
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "ggfProzentDerErneuerbarenEnergieerzeugung",
                            "Gegebenenfalls % der erneuerbaren Energieerzeugung", "%",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Berichterstattung Energieverbrauch von Immobilienvermögen" field
     */
    fun energieeffizienzImmobilienanlagen(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("energieeffizienzImmobilienanlagen") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauchVonImmobilienvermoegen",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "engagementAnteilInEnergieineffizientenImmobilienanlagen",
                            "Engagement/Anteil in energieineffizienten Immobilienanlagen", "",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Berichterstattung Wasserverbrauch" field
     */
    fun berichterstattungWasserverbrauch(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("wasserverbrauch") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungWasserverbrauch",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "wasserverbrauch",
                            "Wasserverbrauch", "l",
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "emissionenInWasser",
                            "Emissionen in Wasser", "t",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Berichterstattung Abfallproduktion" field
     */
    fun unternehmensGruppenStrategieBzglAbfallproduktion(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("abfallproduktion") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungAbfallproduktion",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "gesamteAbfallmenge", "Gesamte Abfallmenge",
                            "t",
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentAbfallRecyclet",
                            "% Abfall recycelt", "%",
                        ),
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentGefaehrlicherAbfall",
                            "% Gefährlicher Abfall", "%",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Recycling im Produktionsprozess" field
     */
    fun recyclingImProduktionsprozess(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("abfallproduktion") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "recyclingImProduktionsprozess",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentRecycelteWerkstoffeImProduktionsprozess",
                            "% Recycelte Werkstoffe im Produktionsprozess",
                            "%",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Berichterstattung Einnahmen aus fossilen Brennstoffen" field
     */
    fun berichterstattungEinnahmenAusFossilenBrennstoffen(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("fossileBrennstoffe") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEinnahmenAusFossilenBrennstoffen",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentDerEinnahmenAusFossilenBrennstoffen",
                            "% der Einnahmen aus fossilen Brennstoffen", "%",
                        ),
                    )
            }
        }
    }

    /**
     * Creates the "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten" field
     */
    fun umsatzInvestitionsaufwandFuerNachhaltige(componentGroupUmwelt: ComponentGroup) {
        componentGroupUmwelt.edit<ComponentGroup>("taxonomie") {
            edit<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent>(
                "umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten",
            ) {
                decimalRows = buildDecimalsRowsForUmsatzInvestitionsaufwandFuerNachhaltige()
            }
        }
    }

    private fun buildDecimalsRowsForUmsatzInvestitionsaufwandFuerNachhaltige():
        MutableList<EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow> =
        mutableListOf(
            EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieGeeignetNachProzentUmsatz",
                "Taxonomie geeignet (eligible) nach % Umsatz",
                "%",
            ),
            EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieGeeignetNachProzentCapex",
                "Taxonomie geeignet (eligible) nach % Capex",
                "%",
            ),
            EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieKonformNachProzentUmsatz",
                "Taxonomie konform (aligned) nach % Umsatz",
                "%",
            ),
            EsgQuestionnaireYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieKonformNachProzentCapex",
                "Taxonomie konform (aligned) nach % Capex",
                "%",
            ),
        )
}
