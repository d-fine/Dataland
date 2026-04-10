package org.dataland.frameworktoolbox.frameworks.eutaxonomyfinancials202673

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.qamodel.FrameworkQaModelBuilder
import org.springframework.stereotype.Component
import java.io.File

/**
 * The EU Taxonomy Financials (2026/73) framework
 */
@Component
class EuTaxonomyFinancials202673Framework :
    PavedRoadFramework(
        identifier = "eutaxonomy-financials-2026-73",
        label = "EU Taxonomy Financials (2026/73)",
        explanation = "EU Taxonomy Financials Framework as of Regulation (EU) 2026/73",
        File("./dataland-framework-toolbox/inputs/eutaxonomy-financials-2026-73/eutaxonomy-financials-2026-73.xlsx"),
        order = 3,
        enabledFeatures = FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.UploadPage),
    ) {
    override fun customizeQaModel(dataModel: FrameworkQaModelBuilder) {
        super.addSupressAnnotationToPackageBuilder(dataModel.rootPackageBuilder, "\"MaxLineLength\"", null)
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("general") {
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
}
