package org.dataland.frameworktoolbox.frameworks.heimathafen

import ComponentGenerationUtilsForGermanFrameworks
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen Framework
 */
@Component
class HeimathafenFramework : PavedRoadFramework(
    identifier = "heimathafen",
    label = "Heimathafen",
    explanation = "Das Heimathafen Framework",
    File("./dataland-framework-toolbox/inputs/heimathafen/dataDictionary-Heimathafen.csv"),
    order = 8,
    enabledFeatures =
    FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.QaModel),
) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return ComponentGenerationUtilsForGermanFrameworks()
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("unternehmen") {
                viewPageExpandOnPageLoad = true
            }
        }
    }
}
