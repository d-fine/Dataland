package org.dataland.frameworktoolbox.frameworks.sfdr

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the SFDR framework
 */
@Component
class SfdrFramework : InDevelopmentPavedRoadFramework(
    identifier = "sfdr",
    label = "SFDR",
    explanation = "Sustainability Finance Disclosure Regulation",
    File("./dataland-framework-toolbox/inputs/sfdr/sfdr.xlsx"),
    order = 5,
) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return SfdrComponentGenerationUtils()
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setSectionColorsAndExpansion(framework.root)
    }

    private fun setSectionColorsAndExpansion(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            viewPageLabelBadgeColor = LabelBadgeColor.Orange
            viewPageExpandOnPageLoad = true

            uploadPageLabelBadgeColor = LabelBadgeColor.Orange
            edit<ComponentGroup>("general") {
                viewPageExpandOnPageLoad = true
            }
        }

        root.edit<ComponentGroup>("environmental") {
            viewPageLabelBadgeColor = LabelBadgeColor.Green
            uploadPageLabelBadgeColor = LabelBadgeColor.Green
        }

        root.edit<ComponentGroup>("social") {
            viewPageLabelBadgeColor = LabelBadgeColor.Yellow
            uploadPageLabelBadgeColor = LabelBadgeColor.Yellow
        }
    }
}
