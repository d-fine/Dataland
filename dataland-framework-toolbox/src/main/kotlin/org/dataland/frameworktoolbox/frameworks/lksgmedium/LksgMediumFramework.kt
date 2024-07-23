package org.dataland.frameworktoolbox.frameworks.lksg

import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.springframework.stereotype.Component
import java.io.File

/**
 * The Lksg Framework
 */
@Component
class LksgMediumFramework : PavedRoadFramework(
    identifier = "lksgmedium",
    label = "LkSGMedium",
    explanation = "Lieferkettensorgfaltspflichtengesetz Medium",
    File("./dataland-framework-toolbox/inputs/lksgMedium/lksgMedium.xlsx"),
    order = 10,
) {

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setSectionColorsAndExpansion(framework.root)
    }

    private fun setSectionColorsAndExpansion(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            viewPageLabelBadgeColor = LabelBadgeColor.Orange
            viewPageExpandOnPageLoad = true
        }

        root.edit<ComponentGroup>("governance") {
            viewPageLabelBadgeColor = LabelBadgeColor.Green
        }
    }
}
