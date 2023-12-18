package org.dataland.frameworktoolbox.frameworks.gdv

import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional

/**
 * This object contains implementations for the rolling window components in the umwelt category of the GDV framework
 */
object GdvUmweltRollingWindowComponents {

    /**
     * Create the "Treibhausgas-Berichterstattung und Prognosen" field
     */
    fun treibhausgasBerichterstattungUndPrognosen(
        componentGroupUmwelt: ComponentGroup,
        available: FrameworkConditional,
    ) {
        val tCo2UnitString = "tCO2-Äquiv."
        componentGroupUmwelt.edit<ComponentGroup>("treibhausgasemissionen") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "treibhausgasBerichterstattungUndPrognosen",
                "treibhausgasEmissionsintensitaetDerUnternehmenInDieInvestriertWird",
            ) {
                label = "Treibhausgas-Berichterstattung und Prognosen"
                explanation = "Welche Treibhausgasinformationen werden derzeit auf Unternehmens-/Konzernebene " +
                    "berichtet und prognostiziert? Bitte geben Sie die Scope1, Scope 2 und Scope 3 Emissionen" +
                    "# für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die " +
                    "kommenden drei Jahre an (in tCO2-Äquiv.)."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "scope1", "Scope 1",
                        tCo2UnitString,
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "scope2", "Scope 2",
                        tCo2UnitString,
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "scope3", "Scope 3",
                        tCo2UnitString,
                    ),
                )
                availableIf = available
            }
        }
    }

    /**
     * Creates the "Berichterstattung Energieverbrauch" field
     */
    fun berichterstattungEnergieverbrauch(componentGroupUmwelt: ComponentGroup, available: FrameworkConditional) {
        componentGroupUmwelt.edit<ComponentGroup>("energieverbrauch") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauch",
                "unternehmensGruppenStrategieBzglEnergieverbrauch",
            ) {
                label = "Berichterstattung Energieverbrauch"
                explanation = "Bitte geben Sie den Energieverbrauch (in GWh), sowie den Verbrauch erneuerbaren " +
                    "Energien (%) und, falls zutreffend, die Erzeugung erneuerbaren Energien (%) für das aktuelle" +
                    " Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "energieverbrauch",
                        "Energieverbrauch", "GWh",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "prozentDesVerbrauchsErneuerbarerEnergien",
                        "% des Verbrauchs erneuerbarer Energien", "%",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "ggfProzentDerErneuerbarenEnergieerzeugung",
                        "Gegebenenfalls % der erneuerbaren Energieerzeugung", "%",
                    ),
                )
                availableIf = available
            }
        }
    }

    /**
     * Creates the "Berichterstattung Energieverbrauch von Immobilienvermögen" field
     */
    fun energieeffizienzImmobilienanlagen(componentGroupUmwelt: ComponentGroup, available: FrameworkConditional) {
        componentGroupUmwelt.edit<ComponentGroup>("energieeffizienzImmobilienanlagen") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauchVonImmobilienvermoegen",
                "unternehmensGruppenStrategieBzglEnergieeffizientenImmobilienanlagen",
            ) {
                label = "Berichterstattung Energieverbrauch von Immobilienvermoegen"
                explanation = "Bitte geben Sie den Anteil an energieeffizienten Immobilienanlagen (%) " +
                        "für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die " +
                        "kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "engagementAnteilInEnergieineffizientenImmobilienanlagen",
                        "Engagement/Anteil in energieineffizienten Immobilienanlagen", "",
                    ),
                )
                availableIf = available
            }
        }
    }

    /**
     * Creates the "Berichterstattung Wasserverbrauch" field
     */
    fun berichterstattungWasserverbrauch(componentGroupUmwelt: ComponentGroup, available: FrameworkConditional) {
        componentGroupUmwelt.edit<ComponentGroup>("wasserverbrauch") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungWasserverbrauch",
                "unternehmensGruppenStrategieBzglWasserverbrauch",
            ) {
                label = "Berichterstattung Wasserverbrauch"
                explanation = "Bitte geben Sie den Wasserverbrauch (in l), sowie die Emissionen in Wasser " +
                        "(in Tonnen) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen " +
                        "für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "wasserverbrauch",
                        "Wasserverbrauch", "l",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "emissionenInWasser",
                        "Emissionen in Wasser", "t",
                    ),

                    )
                availableIf = available
            }
        }
    }

    /**
     * Creates the "Berichterstattung Abfallproduktion" field
     */
    fun unternehmensGruppenStrategieBzglAbfallproduktion(componentGroupUmwelt: ComponentGroup, available: FrameworkConditional) {
        componentGroupUmwelt.edit<ComponentGroup>("abfallproduktion") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungAbfallproduktion",
                "unternehmensGruppenStrategieBzglAbfallproduktion",
            ) {
                label = "Berichterstattung Abfallproduktion"
                explanation = "Bitte geben Sie die gesamte Abfallmenge (in Tonnen), sowie den Anteil (%) " +
                        "der gesamten Abfallmenge, der recyclet wird, sowie den Anteil (%) gefährlicher Abfall der " +
                        "gesamten Abfallmenge für das aktuelle Kalenderjahr, die letzten drei Jahren sowie " +
                        "die Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "gesamteAbfallmenge", "Gesamte Abfallmenge",
                        "t",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "prozentAbfallRecyclet",
                        "% Abfall recycelt", "%",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "prozentGefaehrlicherAbfall",
                        "% Gefährlicher Abfall", "%",
                    ),
                )
                availableIf = available
            }
        }

    }
}
