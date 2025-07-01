package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.SuppressKtlintMaxLineLengthAnnotation
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A DataComponent represents a date (with Year, Month, and Day)
 */
class DateComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(identifier, parent, "java.time.LocalDate") {
    val example = """{
    "value" : "2007-03-05",
    "quality" : "Reported",
    "comment" : "The value is reported by the company.",
    "dataSource" : {
    "page" : "5-7",
    "tagName" : "date",
    "fileName" : "AnnualReport2020.pdf",
    "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
}"""

    override fun getAnnotations(): List<Annotation> {
        val schemaAnnotation =
            Annotation(
                fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                rawParameterSpec =
                    "description = \"\"\"${this.uploadPageExplanation}\"\"\", \n" +
                        "example = \"\"\"$example \"\"\"",
                applicationTargetPrefix = "field",
            )
        return listOf(SuppressKtlintMaxLineLengthAnnotation, schemaAnnotation)
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatStringForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatStringForDatatable",
                            "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val componentName =
            when (documentSupport) {
                is NoDocumentSupport -> "DateFormField"
                is ExtendedDocumentSupport -> "DateExtendedDataPointFormField"
                else ->
                    throw IllegalArgumentException("DateComponent does not support document support '$documentSupport")
            }
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = componentName,
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
