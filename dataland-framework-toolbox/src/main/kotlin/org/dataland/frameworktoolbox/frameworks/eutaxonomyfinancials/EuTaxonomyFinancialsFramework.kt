package org.dataland.frameworktoolbox.frameworks.eutaxonomyfinancials

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.specific.qamodel.FrameworkQaModelBuilder
import org.springframework.stereotype.Component
import java.io.File
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit

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
        order = 1,
        enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
    ) {
    override fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {
        addSupressMaxLineLengthToPackageBuilder(dataModel.rootPackageBuilder)
    }

    override fun customizeQaModel(dataModel: FrameworkQaModelBuilder) {
        addSupressMaxLineLengthToPackageBuilder(dataModel.rootPackageBuilder)
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

    private fun addSupressMaxLineLengthToPackageBuilder(packageBuilder: PackageBuilder) {
        packageBuilder.childElements.forEach { dataModelElement ->
            when (dataModelElement) {
                is PackageBuilder -> {
                    addSupressMaxLineLengthToPackageBuilder(dataModelElement)
                }
                is DataClassBuilder -> {
                    addSuppressMaxLineLengthToDataClass(dataModelElement)
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }

    private fun addSuppressMaxLineLengthToDataClass(dataModelElement: DataClassBuilder) {
        val fullyQualifiedName = "Suppress"
        val rawParameterSpec = "\"MaxLineLength\""

        val index = dataModelElement.annotations.indexOfFirst { it.fullyQualifiedName == fullyQualifiedName }
        if (index >= 0) {
            val oldAnnotation = dataModelElement.annotations[index]
            dataModelElement.annotations[index] =
                Annotation(fullyQualifiedName, "${oldAnnotation.rawParameterSpec}, $rawParameterSpec")
        } else {
            dataModelElement.annotations.add(Annotation(fullyQualifiedName, rawParameterSpec))
        }
    }
}
