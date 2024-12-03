package org.dataland.frameworktoolbox.frameworks.lksg

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.springframework.stereotype.Component
import java.io.File

/**
 * The Lksg Framework
 */
@Component
class LksgFramework :
    PavedRoadFramework(
        identifier = "lksg",
        label = "LkSG",
        explanation = "Lieferkettensorgfaltspflichtengesetz",
        File("./dataland-framework-toolbox/inputs/lksg/lksg.xlsx"),
        order = 4,
        enabledFeatures =
            FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.QaModel, FrameworkGenerationFeatures.DataPointSpecifications),
    ) {
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setSectionColorsAndExpansion(framework.root)
        framework.root
            .get<ComponentGroup>("general")
            .get<ComponentGroup>("masterData")
            .let { parent ->
                editShareOfTemporaryWorkersOptions(parent)
            }
    }

    private fun editShareOfTemporaryWorkersOptions(parent: ComponentGroup) {
        parent.edit<SingleSelectComponent>("shareOfTemporaryWorkers") {
            options =
                mutableSetOf(
                    SelectionOption("Smaller10", "<10%"),
                    SelectionOption("Between10And25", "10-25%"),
                    SelectionOption("Between25And50", "25-50%"),
                    SelectionOption("Greater50", ">50%"),
                )
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

        root.edit<ComponentGroup>("governance") {
            viewPageLabelBadgeColor = LabelBadgeColor.Green
        }

        root.edit<ComponentGroup>("social") {
            viewPageLabelBadgeColor = LabelBadgeColor.Yellow
        }

        root.edit<ComponentGroup>("environmental") {
            viewPageLabelBadgeColor = LabelBadgeColor.Blue
        }

        root.edit<ComponentGroup>("attachment") {
            viewPageLabelBadgeColor = LabelBadgeColor.Brown
            edit<ComponentGroup>("attachment") {
                viewPageExpandOnPageLoad = true
            }
        }
    }
}
