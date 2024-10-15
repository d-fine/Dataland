package org.dataland.frameworktoolbox.frameworks.esgdatenkatalog

import org.dataland.frameworktoolbox.frameworks.esgdatenkatalog.custom
    .EsgDatenkatalogYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit

/**
 * This object contains implementations for the rolling window components in the umwelt category of the ESG
 * Datenkatalog framework
 */
object EsgDatenkatalogUmweltRollingWindowComponents {
    /**
     * Create the "Treibhausgas-Berichterstattung und Prognosen" field
     */
    fun treibhausgasBerichterstattungUndPrognosen(componentGroupUmwelt: ComponentGroup) {
        val tCo2UnitString = "tCO2-Äquiv."
        componentGroupUmwelt.edit<ComponentGroup>("treibhausgasemissionen") {
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "treibhausgasBerichterstattungUndPrognosen",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "scope1", "Scope 1",
                            tCo2UnitString,
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "scope2", "Scope 2",
                            tCo2UnitString,
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauch",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "energieverbrauch",
                            "Energieverbrauch", "GWh",
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentDesVerbrauchsErneuerbarerEnergien",
                            "% des Verbrauchs erneuerbarer Energien", "%",
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauchVonImmobilienvermoegen",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungWasserverbrauch",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "wasserverbrauch",
                            "Wasserverbrauch", "l",
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungAbfallproduktion",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "gesamteAbfallmenge", "Gesamte Abfallmenge",
                            "t",
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                            "prozentAbfallRecyclet",
                            "% Abfall recycelt", "%",
                        ),
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "recyclingImProduktionsprozess",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEinnahmenAusFossilenBrennstoffen",
            ) {
                decimalRows =
                    mutableListOf(
                        EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
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
            edit<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
                "umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten",
            ) {
                decimalRows = buildDecimalsRowsForUmsatzInvestitionsaufwandFuerNachhaltige()
            }
        }
    }

    private fun buildDecimalsRowsForUmsatzInvestitionsaufwandFuerNachhaltige():
        MutableList<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow> =
        mutableListOf(
            EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieGeeignetNachProzentUmsatz",
                "Taxonomie geeignet (eligible) nach % Umsatz",
                "%",
            ),
            EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieGeeignetNachProzentCapex",
                "Taxonomie geeignet (eligible) nach % Capex",
                "%",
            ),
            EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieKonformNachProzentUmsatz",
                "Taxonomie konform (aligned) nach % Umsatz",
                "%",
            ),
            EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                "taxonomieKonformNachProzentCapex",
                "Taxonomie konform (aligned) nach % Capex",
                "%",
            ),
        )
}
