package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancialsFramework :
    PavedRoadFramework(
        identifier = "eutaxonomy-non-financials",
        label = "EU Taxonomy Non-Financials",
        explanation = "The EU-Taxonomy framework for non-financial companies",
        File(
            "./dataland-framework-toolbox/inputs/euTaxonomyNonFinancials/EutaxonomyNonFinancials.xlsx",
        ),
        order = 3,
        enabledFeatures =
            FrameworkGenerationFeatures
                .allExcept(FrameworkGenerationFeatures.UploadPage),
    ) {
    override fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {
        val tooLargeClasses =
            listOf(
                "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.EutaxonomyNonFinancialsCapex",
                "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.revenue.EutaxonomyNonFinancialsRevenue",
            )
        addSupressAnnotationToPackageBuilder(dataModel.rootPackageBuilder, "\"LargeClass\"", tooLargeClasses)
    }

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
        framework.root.edit<ComponentGroup>("general") {
            edit<ReportPreuploadComponent>("referencedReports") {
                isPartOfQaReport = false
            }
            edit<SingleSelectComponent>("fiscalYearDeviation") {
                specificationGenerator = { categoryBuilder ->
                    categoryBuilder.addDefaultDatapointAndSpecification(
                        this,
                        "Enum",
                        "extendedEnumFiscalYearDeviation",
                    )
                }
            }
            edit<YesNoComponent>("nfrdMandatory") {
                specificationGenerator = { categoryBuilder ->
                    val nfrdMandatoryComponent = this
                    nfrdMandatoryComponent.label = "Is NFRD mandatory?"
                    categoryBuilder.addDefaultDatapointAndSpecification(
                        nfrdMandatoryComponent,
                        "EnumYesNo",
                    )
                }
            }
        }
        configureComponentGroupColorsAndExpansion(framework.root)
    }
}
