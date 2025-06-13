package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-specific "NuclearAndGasEligibleButNotAligned" component
 */
class NuclearAndGasEligibleButNotAlignedComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    private val fullyQualifiedNameOfKotlinType =
        "org.dataland.datalandbackend.frameworks.nuclearandgas.custom.NuclearAndGasEligibleButNotAligned"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "NuclearAndGasFormElement",
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatNuclearAndGasTaxonomyShareDataForTable(" +
                    "${getTypescriptFieldAccessor(true)}, \"${
                        StringEscapeUtils.escapeEcmaScript(
                            label,
                        )
                    }\")",
                setOf(
                    TypeScriptImport(
                        "formatNuclearAndGasTaxonomyShareDataForTable",
                        "@/components/resources/dataTable/conversion/" +
                            "NuclearAndGasValueGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.randomExtendedDataPoint(" +
                "dataGenerator.generateNuclearAndGasEligibleButNotAligned())",
        )
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "NuclearAndGasEligibleButNotAlignedComponent",
        )
    }
}
