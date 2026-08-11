package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.JsonExamples.EXAMPLE_PLAIN_DATE_COMPONENT
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * A DateComponent represents a date (with Year, Month, and Day)
 */
class DateComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(identifier, parent, "java.time.LocalDate") {
    override fun getAnnotations(): List<Annotation> =
        getSchemaAnnotationWithSuppressMaxLineLength(
            uploadPageExplanation,
            getExample(EXAMPLE_PLAIN_DATE_COMPONENT),
        )

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) =
        addSingleArgumentFormatterCell(
            sectionConfigBuilder,
            formatterFunction = "formatStringForDatatable",
            formatterModule = "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
            dataPointCastType = "string",
        )

    override fun getUploadComponentName(): String =
        when (documentSupport) {
            is NoDocumentSupport -> "DateFormField"
            is ExtendedDocumentSupport -> "DateExtendedDataPointFormField"
            else -> throw IllegalArgumentException("DateComponent does not support document support '$documentSupport")
        }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedFutureDate()",
                nullableFixtureExpression = "dataGenerator.randomFutureDate()",
                nullable = isNullable,
            ),
        )
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport, ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "Date",
        )
    }
}
