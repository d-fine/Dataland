package org.dataland.frameworktoolbox.frameworks.lksg

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.springframework.stereotype.Component
import java.io.File

@Component
class LksgFramework : InDevelopmentPavedRoadFramework(
    identifier = "lksg",
    label = "LkSG",
    explanation = "Lieferkettensorgfaltspflichtengesetz",
    File("./dataland-framework-toolbox/inputs/lksg/lksg.xlsx"),
    order = 6,
) {

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setSectionColorsAndExpansion(framework.root)
        val masterData = framework.root.getOrNull<ComponentGroup>("general")
            ?.getOrNull<ComponentGroup>("masterData")
        if (masterData != null) {
            editShareOfTemporaryWorkersOptions(masterData)
        }
    }

    private fun editShareOfTemporaryWorkersOptions(component: ComponentGroup) {
        component.edit<SingleSelectComponent>("shareOfTemporaryWorkers") {
            options = mutableSetOf( SelectionOption("Smaller10","<10%"),
                SelectionOption("Between10And25", "10-25%"),
        SelectionOption("Between25And50", "25-50%"),
        SelectionOption( "Greater50", ">50%"))
        }
    }

    private fun setSectionColorsAndExpansion(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            viewPageLabelBadgeColor = LabelBadgeColor.Orange
            viewPageExpandOnPageLoad = true

            uploadPageLabelBadgeColor = LabelBadgeColor.Orange
            edit<ComponentGroup>("masterData") {
                viewPageExpandOnPageLoad = true
            }
        }
    }
}
