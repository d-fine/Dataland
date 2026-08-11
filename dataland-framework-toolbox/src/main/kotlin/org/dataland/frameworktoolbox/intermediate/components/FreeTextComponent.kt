package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.JsonExamples.EXAMPLE_PLAIN_FREE_TEXT_COMPONENT
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * A FreeTextComponent represents an arbitrary textual value that may contain multiple lines or even
 * paragraphs
 */
class FreeTextComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(identifier, parent, "String") {
    override fun getAnnotations(): List<Annotation> =
        getSchemaAnnotationWithSuppressMaxLineLength(
            uploadPageExplanation,
            getExample(EXAMPLE_PLAIN_FREE_TEXT_COMPONENT),
        )

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) =
        addSingleArgumentFormatterCell(
            sectionConfigBuilder,
            formatterFunction = "formatFreeTextForDatatable",
            formatterModule = "@/components/resources/dataTable/conversion/FreeTextValueGetterFactory",
            dataPointCastType = "string",
        )

    override fun getUploadComponentName(): String = "FreeTextFormField"

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
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
