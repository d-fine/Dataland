package org.dataland.frameworktoolbox.frameworks.gdv

import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
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
                "treibhausgasEmissionsintensitaetDerUnternehmenInDieInvestiertWird",
            ) {
                label = "Treibhausgas-Berichterstattung und Prognosen"
                explanation = "Welche Treibhausgasinformationen werden derzeit auf Unternehmens-/Konzernebene " +
                    "berichtet und prognostiziert? Bitte geben Sie die Scope1, Scope 2 und Scope 3 Emissionen" +
                    "# für das aktuelle Kalenderjahr, die letzten drei Jahre sowie die Prognosen für die " +
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
                    " Kalenderjahr, die letzten drei Jahre sowie die Prognosen für die kommenden drei Jahre an."
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
                    "für das aktuelle Kalenderjahr, die letzten drei Jahre sowie die Prognosen für die " +
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
                    "(in Tonnen) für das aktuelle Kalenderjahr, die letzten drei Jahre sowie die Prognosen " +
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
    fun unternehmensGruppenStrategieBzglAbfallproduktion(
        componentGroupUmwelt: ComponentGroup,
        available: FrameworkConditional,
    ) {
        componentGroupUmwelt.edit<ComponentGroup>("abfallproduktion") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungAbfallproduktion",
                "unternehmensGruppenStrategieBzglAbfallproduktion",
            ) {
                label = "Berichterstattung Abfallproduktion"
                explanation = "Bitte geben Sie die gesamte Abfallmenge (in Tonnen), sowie den Anteil (%) " +
                    "der gesamten Abfallmenge, der recyclet wird, sowie den Anteil (%) gefährlicher Abfall der " +
                    "gesamten Abfallmenge für das aktuelle Kalenderjahr, die letzten drei Jahre sowie " +
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

    /**
     * Creates the "Recycling im Produktionsprozess" field
     */
    fun recyclingImProduktionsprozess(componentGroupUmwelt: ComponentGroup, available: FrameworkConditional) {
        componentGroupUmwelt.edit<ComponentGroup>("abfallproduktion") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "recyclingImProduktionsprozess",
                "gefaehrlicherAbfall",
            ) {
                label = "Recycling im Produktionsprozess"
                explanation = "Bitte geben Sie an, wie hoch der Anteil an Recyclaten (bereits" +
                    "recyceltes wiederverwertetes Material) im Produktionsprozess für das aktuelle Kalenderjahr, " +
                    "die letzten drei Jahre sowie die Prognosen für die kommenden drei Jahre."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "prozentRecycelteWerkstoffeImProduktionsprozess",
                        "% Recycelte Werkstoffe im Produktionsprozess",
                        "%",
                    ),
                )
                availableIf = available
            }
        }
    }

    /**
     * Creates the "Berichterstattung Einnahmen aus fossilen Brennstoffen" field
     */
    fun berichterstattungEinnahmenAusFossilenBrennstoffen(componentGroupUmwelt: ComponentGroup) {
        val profitFromFossilFuels = componentGroupUmwelt
            .get<ComponentGroup>("fossileBrennstoffe")
            .get<YesNoComponent>("einnahmenAusFossilenBrennstoffen")

        componentGroupUmwelt.edit<ComponentGroup>("fossileBrennstoffe") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEinnahmenAusFossilenBrennstoffen",
            ) {
                label = "Berichterstattung Einnahmen aus fossilen Brennstoffen"
                explanation = "Bitte geben Sie den Anteil (%) der Einnahmen aus fossilen Brennstoffen aus den " +
                    "gesamten Einnahmen für das aktuelle Kalenderjahr, die letzten drei Jahre sowie die " +
                    "Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "prozentDerEinnahmenAusFossilenBrennstoffen",
                        "% der Einnahmen aus fossilen Brennstoffen", "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    profitFromFossilFuels,
                    "Yes",
                )
            }
        }
    }

    /**
     * Creates the "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten" field
     */
    fun umsatzInvestitionsaufwandFuerNachhaltige(
        componentGroupUmwelt: ComponentGroup,
        available: FrameworkConditional,
    ) {
        componentGroupUmwelt.edit<ComponentGroup>("taxonomie") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "umsatzInvestitionsaufwandFuerNachhaltige" +
                    "Aktivitaeten",
            ) {
                label = "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten"
                explanation = "Wie hoch ist der Umsatz/Investitionsaufwand des Unternehmens aus nachhaltigen " +
                    "Aktivitäten (Mio. €) gemäß einer Definition der EU-Taxonomie? Bitte machen Sie Angaben " +
                    "zu den betrachteten Sektoren und gegebenenfalls zu den Annahmen bzgl. Taxonomie-konformen" +
                    " (aligned) Aktivitäten für das aktuelle Kalenderjahr, die letzten drei Jahre sowie die " +
                    "Prognosen für die kommenden drei Jahre an."
                decimalRows = buildDecimalsRowsForUmsatzInvestitionsaufwandFuerNachhaltige()
                availableIf = available
            }
        }
    }

    private fun buildDecimalsRowsForUmsatzInvestitionsaufwandFuerNachhaltige():
        MutableList<GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow> {
        return mutableListOf(
            GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieGeeignetNachProzentUmsatz",
                "Taxonomie geeignet (eligible) nach % Umsatz",
                "%",
            ),
            GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieGeeignetNachProzentCapex",
                "Taxonomie geeignet (eligible) nach % Capex",
                "%",
            ),
            GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieKonformNachProzentUmsatz",
                "Taxonomie konform (aligned) nach % Umsatz",
                "%",
            ),
            GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieKonformNachProzentCapex",
                "Taxonomie konform (aligned) nach % Capex",
                "%",
            ),
        )
    }
}
