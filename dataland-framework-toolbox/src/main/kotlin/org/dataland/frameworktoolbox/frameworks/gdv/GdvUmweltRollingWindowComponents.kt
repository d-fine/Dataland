package org.dataland.frameworktoolbox.frameworks.gdv

import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
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
}
