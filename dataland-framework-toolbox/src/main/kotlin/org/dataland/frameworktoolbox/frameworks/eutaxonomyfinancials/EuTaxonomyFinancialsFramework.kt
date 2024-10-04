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

/**
 * The EU Taxonomy Financials framework
 */
@Component
class EuTaxonomyFinancialsFramework : PavedRoadFramework(
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
                    throw IllegalArgumentException(
                        "Unsupported data model element type: ${dataModelElement::class.simpleName}",
                    )
                }
            }
        }
    }

    private fun addSuppressMaxLineLengthToDataClass(
        dataModelElement: DataClassBuilder,
    ) {
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
