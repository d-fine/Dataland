package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials202673

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.qamodel.FrameworkQaModelBuilder
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancials202673Framework :
    InDevelopmentPavedRoadFramework(
        identifier = "eutaxonomy-non-financials-2026-73",
        label = "EU Taxonomy Non-Financials (2026/73)",
        explanation = "The EU-Taxonomy framework for non-financial companies as of Regulation (EU) 2026/73",
        File("./dataland-framework-toolbox/inputs/eutaxonomy-non-financials-2026-73/eutaxonomy-non-financials-2026-73.xlsx"),
        order = 5,
        enabledFeatures = FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.UploadPage),
    ) {
    override fun customizeQaModel(dataModel: FrameworkQaModelBuilder) {
        addSupressAnnotationToPackageBuilder(dataModel.rootPackageBuilder, "\"MaxLineLength\"", null)
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
        }
    }
}
