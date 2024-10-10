package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getKotlinFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-Specific "Assurance" component
 */
class EuTaxonomyAssuranceComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    private val fullyQualifiedNameOfKotlinType =
        "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.AssuranceDataPoint"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
            listOf(ValidAnnotation),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
            listOf(ValidAnnotation),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatAssuranceForDataTable(${getTypescriptFieldAccessor(true)}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        "formatAssuranceForDataTable",
                        "@/components/resources/dataTable/conversion/EutaxonomyAssuranceValueGetterFactory",
                    ),
                ),
            ),
        )
        createNewViewConfigCellForAssuranceProvider(sectionConfigBuilder)
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "AssuranceFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        val guaranteedAssuranceDataPointGenerator = "dataGenerator.generateAssuranceDatapoint()"
        val fixtureExpression =
            if (isNullable) {
                "dataGenerator.valueOrNull($guaranteedAssuranceDataPointGenerator)"
            } else {
                guaranteedAssuranceDataPointGenerator
            }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExpression,
        )
    }

    private fun createNewViewConfigCellForAssuranceProvider(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addCell(
            label = "Assurance Provider",
            explanation = "Provider of the Assurance",
            shouldDisplay = availableIf.toFrameworkBooleanLambda(),
            valueGetter =
                FrameworkDisplayValueLambda(
                    "formatAssuranceProviderForDataTable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatAssuranceProviderForDataTable",
                            "@/components/resources/dataTable/conversion/EutaxonomyAssuranceValueGetterFactory",
                        ),
                    ),
                ),
        )
    }

    override fun getExtendedDocumentReference(): List<String> = listOf("${this.getKotlinFieldAccessor()}?.dataSource?.fileReference")
}
