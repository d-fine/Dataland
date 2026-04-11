package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import java.io.File

/**
 * Configuration for EU Taxonomy Non-Financials framework initialization.
 */
data class EuTaxonomyNonFinancialsFrameworkConfig(
    val identifier: String,
    val label: String,
    val explanation: String,
    val frameworkTemplateCsvFile: File,
    val order: Int,
)

/**
 * Shared base class for EU Taxonomy Non-Financials framework variants.
 */
abstract class EuTaxonomyNonFinancialsBaseFramework(
    frameworkConfig: EuTaxonomyNonFinancialsFrameworkConfig,
    enabledFeatures: Set<FrameworkGenerationFeatures>,
    private val tooLargeClasses: List<String> = emptyList(),
    private val includeNfrdMandatory: Boolean = false,
) : PavedRoadFramework(
        identifier = frameworkConfig.identifier,
        label = frameworkConfig.label,
        explanation = frameworkConfig.explanation,
        frameworkTemplateCsvFile = frameworkConfig.frameworkTemplateCsvFile,
        order = frameworkConfig.order,
        enabledFeatures = enabledFeatures,
    ) {
    override fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {
        if (tooLargeClasses.isNotEmpty()) {
            addSupressAnnotationToPackageBuilder(dataModel.rootPackageBuilder, "\"LargeClass\"", tooLargeClasses)
        }
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
            if (includeNfrdMandatory) {
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
        }
        configureComponentGroupColorsAndExpansion(framework.root)
    }
}
