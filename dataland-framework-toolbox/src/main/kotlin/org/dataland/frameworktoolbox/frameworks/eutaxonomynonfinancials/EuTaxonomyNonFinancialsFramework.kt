package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancialsFramework : PavedRoadFramework(
    identifier = "eutaxonomy-non-financials",
    label = "EU Taxonomy Non-Financials",
    explanation = "The EU-Taxonomy framework for non-financial companies",
    File(
        "./dataland-framework-toolbox/inputs/euTaxonomyNonFinancials/EutaxonomyNonFinancials.xlsx",
    ),
    order = 2,
    enabledFeatures =
    FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.UploadPage, FrameworkGenerationFeatures.QaModel),
) {

    private fun configureComponentGroupColorsAndExpansion(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            uploadPageLabelBadgeColor = LabelBadgeColor.Orange
            viewPageLabelBadgeColor = LabelBadgeColor.Orange
        }

        root.edit<ComponentGroup>("revenue") {
            uploadPageLabelBadgeColor = LabelBadgeColor.Green
            viewPageLabelBadgeColor = LabelBadgeColor.Green
        }

        root.edit<ComponentGroup>("capex") {
            uploadPageLabelBadgeColor = LabelBadgeColor.Yellow
            viewPageLabelBadgeColor = LabelBadgeColor.Yellow
        }

        root.edit<ComponentGroup>("opex") {
            uploadPageLabelBadgeColor = LabelBadgeColor.Blue
            viewPageLabelBadgeColor = LabelBadgeColor.Blue
        }
    }
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        configureComponentGroupColorsAndExpansion(framework.root)
    }
}
