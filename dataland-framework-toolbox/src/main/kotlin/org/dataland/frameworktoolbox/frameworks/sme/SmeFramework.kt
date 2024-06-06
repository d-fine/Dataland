package org.dataland.frameworktoolbox.frameworks.sme

import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the SME framework
 */
@Component
class SmeFramework : PavedRoadFramework(
    identifier = "sme",
    label = "SME",
    explanation = "Small and medium-sized enterprises questionnaire",
    File("./dataland-framework-toolbox/inputs/sme/sme.xlsx"),
    order = 6,
    isPrivateFramework = true,
) {
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("basicInformation") {
                viewPageExpandOnPageLoad = true
            }
        }
    }
}
