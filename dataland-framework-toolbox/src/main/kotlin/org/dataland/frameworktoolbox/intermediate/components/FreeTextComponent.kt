package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.JsonExamples.EXAMPLE_PLAIN_FREE_TEXT_COMPONENT
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.SuppressKtlintMaxLineLengthAnnotation
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A FreeTextComponent represents an arbitrary textual value that may contain multiple lines or even
 * paragraphs
 */
class FreeTextComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(identifier, parent, "String") {
    override fun getAnnotations(): List<org.dataland.frameworktoolbox.specific.datamodel.Annotation> {
        val schemaAnnotation =
            Annotation(
                fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                rawParameterSpec =
                    "description = \"\"\"${this.uploadPageExplanation}\"\"\", \n" +
                        "example = \"\"\"${getExample(EXAMPLE_PLAIN_FREE_TEXT_COMPONENT)} \"\"\"",
                applicationTargetPrefix = "field",
            )
        return listOf(SuppressKtlintMaxLineLengthAnnotation, schemaAnnotation)
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatFreeTextForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatFreeTextForDatatable",
                            "@/components/resources/dataTable/conversion/FreeTextValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "FreeTextFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedParagraphs()",
                nullableFixtureExpression = "dataGenerator.randomParagraphs()",
                nullable = isNullable,
            ),
        )
    }
}
