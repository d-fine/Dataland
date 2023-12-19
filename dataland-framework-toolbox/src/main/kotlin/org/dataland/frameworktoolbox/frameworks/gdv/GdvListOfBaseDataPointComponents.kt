package org.dataland.frameworktoolbox.frameworks.gdv

import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvListOfBaseDataPointComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional

object GdvListOfBaseDataPointComponents {

    /**
     * Creates the "Aktuelle Berichte" component
     */
    fun aktuelleBerichte(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein.edit<ComponentGroup>("esgBerichte") {
            val nachhaltigkeitsberichte = getOrNull<YesNoComponent>("nachhaltigkeitsberichte")
            requireNotNull(nachhaltigkeitsberichte)

            create<GdvListOfBaseDataPointComponent>("aktuelleBerichte") {
                label = "Aktuelle Berichte"
                explanation = "Aktuelle Nachhaltigkeits- oder ESG-Berichte"
                descriptionColumnHeader = "Beschreibung des Berichts"
                documentColumnHeader = "Bericht"
                availableIf = DependsOnComponentValue(
                    nachhaltigkeitsberichte,
                    "Yes",
                )
            }
        }
    }

    /**
     * Creates the "Weitere Akkreditierungen" component
     */
    fun weitereAkkreditierungen(
        componentGroupAllgemein: ComponentGroup,
        available: FrameworkConditional,
    ) {
        componentGroupAllgemein.edit<ComponentGroup>("akkreditierungen") {
            create<GdvListOfBaseDataPointComponent>(
                "weitereAkkreditierungen",
            ) {
                label = "Weitere Akkreditierungen"
                explanation = "Weitere Akkreditierungen, die noch nicht aufgeführt wurden"
                descriptionColumnHeader = "Beschreibung der Akkreditierung"
                documentColumnHeader = "Akkreditierung"
                availableIf = available
            }
        }
    }
}
