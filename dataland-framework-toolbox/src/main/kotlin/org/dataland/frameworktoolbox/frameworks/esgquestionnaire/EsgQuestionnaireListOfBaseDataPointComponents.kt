package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import org.dataland.frameworktoolbox.frameworks.esgquestionnaire.custom.EsgQuestionnaireListOfBaseDataPointComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional

/**
 * This object contains implementations for the list of base datapoint components of the GDV framework
 */
object EsgQuestionnaireListOfBaseDataPointComponents {

    /**
     * Creates the "Aktuelle Berichte" component
     */
    fun aktuelleBerichte(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein.edit<ComponentGroup>("esgBerichte") {
            val nachhaltigkeitsberichte = get<YesNoComponent>("nachhaltigkeitsberichte")

            create<EsgQuestionnaireListOfBaseDataPointComponent>("aktuelleBerichte") {
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
            create<EsgQuestionnaireListOfBaseDataPointComponent>(
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

    /**
     * Creates the "Richtlinien zur Einhaltung der UNGCP" component
     */
    fun richtlinienZurEinhaltungDerUngcp(
        componentGroupAllgemein: ComponentGroup,
    ) {
        componentGroupAllgemein.edit<ComponentGroup>("unGlobalConceptPrinzipien") {
            val mechanismenZurUeberwachungDerEinhaltungDerUngcp =
                get<YesNoComponent>("mechanismenZurUeberwachungDerEinhaltungDerUngcp")
            create<EsgQuestionnaireListOfBaseDataPointComponent>(
                "richtlinienZurEinhaltungDerUngcp",
                "erklaerungDerEinhaltungDerUngcp",
            ) {
                label = "Richtlinien zur Einhaltung der UNGCP"
                explanation = "Bitte teilen Sie die Richtlinien mit uns die beschreiben oder Informationen darüber " +
                    "liefern, wie das Unternehmen die Einhaltung der UN Global Compact Prinzipien überwacht."
                descriptionColumnHeader = "Beschreibung der Richtlinie"
                documentColumnHeader = "Richtlinie"
                availableIf = DependsOnComponentValue(mechanismenZurUeberwachungDerEinhaltungDerUngcp, "Yes")
            }
        }
    }

    /**
     * Creates the "Richtlinien zur Einhaltung der OECD-Leitsätze" component
     */
    fun richtlinienZurEinhaltungDerOecdLeitsaetze(
        componentGroupAllgemein: ComponentGroup,
    ) {
        componentGroupAllgemein.edit<ComponentGroup>("oecdLeitsaetze") {
            val mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze =
                get<YesNoComponent>("mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze")

            create<EsgQuestionnaireListOfBaseDataPointComponent>(
                "richtlinienZurEinhaltungDerOecdLeitsaetze",
                "erklaerungDerEinhaltungDerOecdLeitsaetze",
            ) {
                label = "Richtlinien zur Einhaltung der OECD-Leitsätze"
                explanation = "Bitte teilen Sie die Richtlinien mit uns die beschreiben oder Informationen darüber " +
                    "liefern, wie das Unternehmen die Einhaltung der OECD-Leitsätze überwacht."
                descriptionColumnHeader = "Beschreibung der Richtlinie"
                documentColumnHeader = "Richtlinie"
                availableIf = DependsOnComponentValue(mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze, "Yes")
            }
        }
    }
}
