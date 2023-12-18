package org.dataland.frameworktoolbox.frameworks.gdv

import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue

/**
 * This object contains implementations for the rolling window components in the soziales category of the GDV framework
 */
object GdvSozialesRollingWindowComponents {
    /**
     * Creates the "Auswirkungen auf Anteil befrister Verträge und Fluktuation" field
     */
    fun auswirkungenAufAnteilBefristerVertraegeUndFluktuation(
        componentGroupSoziales: ComponentGroup,
    ) {
        componentGroupSoziales.edit<ComponentGroup>("unternehmensstrukturaenderungen") {
            val vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur =
                getOrNull<YesNoComponent>("vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur")!!

            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
            ) {
                label = "Auswirkungen auf Anteil befrister Verträge und Fluktuation"
                explanation = "Bitte geben Sie die Anzahl der befristeten Verträge sowie die Fluktuation (%) für die" +
                    " letzten drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anzahlDerBefristetenVertraege",
                        "# der befristeten Verträge", "",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "fluktuation", "Fluktuation",
                        "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur,
                    "Yes",
                )
                uploadBehaviour = GdvYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearPast
            }
        }
    }
}
