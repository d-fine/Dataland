package org.dataland.frameworktoolbox.frameworks.sfdr

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
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
class SfdrFramework :
    PavedRoadFramework(
        identifier = "sfdr",
        label = "SFDR",
        explanation = "Sustainability Finance Disclosure Regulation",
        File("./dataland-framework-toolbox/inputs/sfdr/sfdr.xlsx"),
        order = 1,
        enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
    ) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils = SfdrComponentGenerationUtils()

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("general") {
                edit<ReportPreuploadComponent>("referencedReports") {
                    isPartOfQaReport = false
                }
            }
        }
        setSectionColorsAndExpansion(framework.root)
        overwriteDataPointSpecificationForEnums(framework.root)
    }

    private fun overwriteDataPointSpecificationForEnums(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("general") {
                edit<SingleSelectComponent>("fiscalYearDeviation") {
                    specificationGenerator = { categoryBuilder ->
                        categoryBuilder.addDefaultDatapointAndSpecification(
                            this,
                            "Enum",
                            "plainEnumFiscalYearDeviation",
                        )
                    }
                }
                edit<DateComponent>("dataDate") {
                    specificationGenerator = { categoryBuilder ->
                        categoryBuilder.addDefaultDatapointAndSpecification(
                            this,
                            "DateSfdr",
                            "plainDate",
                        )
                    }
                }
            }
        }
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
