package org.dataland.frameworktoolbox.frameworks.esgdatenkatalog

import org.dataland.frameworktoolbox.intermediate.components.ListOfStringBaseDataPointComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get

/**
 * This object contains implementations for the list of base datapoint components of the ESG Datenkatalog framework
 */
object EsgDatenkatalogListOfStringBaseDataPointComponents {
    /**
     * Sets german headers for the "Dokumente zur Nachhaltigkeitsstrategie" component
     */
    fun dokumenteZurNachhaltigkeitsstrategie(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein
            .get<ComponentGroup>("generelleEsgStrategie")
            .edit<ListOfStringBaseDataPointComponent>("dokumenteZurNachhaltigkeitsstrategie") {
                descriptionColumnHeader = "Beschreibung des Dokuments zur Nachhaltigkeitsstrategie"
                documentColumnHeader = "Dokument"
            }
    }

    /**
     * Sets german headers for the "Richtlinien zur Einhaltung der UNGCP" component
     */
    fun richtlinienZurEinhaltungDerUngcp(componentGroupAllgemein: ComponentGroup) {
        componentGroupAllgemein
            .get<ComponentGroup>("unGlobalCompactPrinzipien")
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
