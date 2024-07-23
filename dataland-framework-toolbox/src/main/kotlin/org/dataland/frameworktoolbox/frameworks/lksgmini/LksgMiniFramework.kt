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
class LksgMiniFramework : PavedRoadFramework(
    identifier = "lksgmini",
    label = "LkSGMini",
    explanation = "Lieferkettensorgfaltspflichtengesetz Mini",
    File("./dataland-framework-toolbox/inputs/lksgMini/lksgMini.xlsx"),
    order = 9,
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
