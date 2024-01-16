package org.dataland.frameworktoolbox.frameworks.heimathafen

import HeimathafenComponentGenerationUtils
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
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
class HeimathafenFramework : InDevelopmentPavedRoadFramework(
    identifier = "heimathafen",
    label = "Heimathafen",
    explanation = "Das Heimathafen Framework",
    File("./dataland-framework-toolbox/inputs/gdv/dataDictionary-GDV-VOEB-GDV-VÖB ESG questionnaire.csv"),
) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return HeimathafenComponentGenerationUtils()
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        super.customizeHighLevelIntermediateRepresentation(framework)

        framework.root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("masterData") {
                viewPageExpandOnPageLoad = true
            }
        }
    }
}
