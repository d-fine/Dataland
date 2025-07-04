package org.dataland.frameworktoolbox.frameworks.eutaxonomyfinancials

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
 * The EU Taxonomy Financials framework
 */
@Component
class EuTaxonomyFinancialsFramework :
    PavedRoadFramework(
        identifier = "eutaxonomy-financials",
        label = "EU Taxonomy Financials",
        explanation = "Additional Taxonomy for Financials",
        File("./dataland-framework-toolbox/inputs/eu-taxonomy-financials/eu-taxonomy-financials.xlsx"),
        order = 2,
        enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
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
