package org.dataland.frameworktoolbox.frameworks.heimathafen

import ComponentGenerationUtilsForGermanFrameworks
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.heimathafen.custom.HeimathafenListOfBaseDataPointComponent
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen Framework
 */
@Component
class HeimathafenFramework : InDevelopmentPavedRoadFramework(
    identifier = "heimathafen",
    label = "Heimathafen",
    explanation = "Das Heimathafen Framework",
    File("./dataland-framework-toolbox/inputs/heimathafen/dataDictionary-Heimathafen.csv"),
) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return ComponentGenerationUtilsForGermanFrameworks()
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        super.customizeHighLevelIntermediateRepresentation(framework)

        framework.root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("datenanbieter") {
                viewPageExpandOnPageLoad = true
            }
        }
        val componentGroupGeneral = framework.root.getOrNull<ComponentGroup>("General")
        if (componentGroupGeneral != null) {
            componentGroupGeneral.getOrNull<ComponentGroup>("Methodik")
                ?.create<HeimathafenListOfBaseDataPointComponent>("datenquelle") {
                    label = "Datenquelle"
                    explanation = "Welche Quellen werden für die Datenerhebung verwendet? Angabe von Quellen für die" +
                        " Datenerhebung, zum Beispiel Nachhaltigkeitsberichte von Unternehmen, Daten von NGOs etc."
                    descriptionColumnHeader = "Beschreibung des Berichts"
                    documentColumnHeader = "Datenquelle"
                }
        }

        framework.root.getOrNull<ComponentGroup>("Environmental")
            ?.getOrNull<ComponentGroup>("Nachhaltigskeitsrisiken")
            ?.create<HeimathafenListOfBaseDataPointComponent>("quelle") {
                label = "Quelle"
                explanation = "Welche Quellen werden für die Erfassung von Nachhaltigkeitsrisiken im Bereich Umwelt" +
                    "verwendet? Angabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen," +
                    " Daten von NGOs etc."
                documentColumnHeader = "Quelle"
            }

        removeAntiDuplicationStringsForIdentifiers(framework)
    }
}

private fun removeAntiDuplicationStringsForIdentifiers(framework: Framework) {
    val keineArmutComponentGroup = framework.root.getOrNull<ComponentGroup>("sdg")
        ?.getOrNull<ComponentGroup>("keineArmut")
    keineArmutComponentGroup?.edit<YesNoComponent>("sdgKeineArmut") {
        val oldLabel = label.toString()
        print(oldLabel)
        label = "abctest"
        explanation = "deftestHU"
    }
}
