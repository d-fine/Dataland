package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import org.dataland.frameworktoolbox.intermediate.components.ListOfStringBaseDataPointComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get

/**
 * This object contains implementations for the list of base datapoint components of the ESG Questionnaire framework
 */
object EsgQuestionnaireListOfStringBaseDataPointComponents {
    /**
     * Sets german headers for the "Aktuelle Berichte" component
     */
    fun aktuelleBerichte(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein
            .get<ComponentGroup>("esgBerichte")
            .edit<ListOfStringBaseDataPointComponent>("aktuelleBerichte") {
                descriptionColumnHeader = "Beschreibung des Berichts"
                documentColumnHeader = "Bericht"
            }
    }

    /**
     * Sets german headers for the "Weitere Akkreditierungen" component
     */
    fun weitereAkkreditierungen(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein
            .get<ComponentGroup>("akkreditierungen")
            .edit<ListOfStringBaseDataPointComponent>("weitereAkkreditierungen") {
                descriptionColumnHeader = "Beschreibung der Akkreditierung"
                documentColumnHeader = "Akkreditierung"
            }
    }

    /**
     * Sets german headers for the "Richtlinien zur Einhaltung der UNGCP" component
     */
    fun richtlinienZurEinhaltungDerUngcp(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein
            .get<ComponentGroup>("unGlobalConceptPrinzipien")
            .edit<ListOfStringBaseDataPointComponent>("richtlinienZurEinhaltungDerUngcp") {
                descriptionColumnHeader = "Beschreibung der Richtlinie"
                documentColumnHeader = "Richtlinie"
            }
    }

    /**
     * Sets german headers for the "Richtlinien zur Einhaltung der OECD-Leitsätze" component
     */
    fun richtlinienZurEinhaltungDerOecdLeitsaetze(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein
            .get<ComponentGroup>("oecdLeitsaetze")
            .edit<ListOfStringBaseDataPointComponent>("richtlinienZurEinhaltungDerOecdLeitsaetze") {
                descriptionColumnHeader = "Beschreibung der Richtlinie"
                documentColumnHeader = "Richtlinie"
            }
    }
}
