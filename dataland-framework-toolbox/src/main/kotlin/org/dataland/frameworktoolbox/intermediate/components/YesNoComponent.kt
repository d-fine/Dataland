package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.SimpleKotlinBackedBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
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
 * A YesNoComponent is either Yes or No.
 */
class YesNoComponent(
    identifier: String,
    parent: FieldNodeParent,
) : SimpleKotlinBackedBaseComponent(identifier, parent, "org.dataland.datalandbackend.model.enums.commons.YesNo") {
    val example = """{
    "value" : "Yes",
    "quality" : "Reported",
    "comment" : "The value is reported by the company.",
    "dataSource" : {
      "page" : "5-7",
      "tagName" : "date",
      "fileName" : "AnnualReport2020.pdf",
      "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    }
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
                    "formatYesNoValueForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatYesNoValueForDatatable",
                            "@/components/resources/dataTable/conversion/YesNoValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val uploadComponentNameToUse =
            when (documentSupport) {
                is NoDocumentSupport -> "YesNoFormField"
                is SimpleDocumentSupport -> "YesNoBaseDataPointFormField"
                is ExtendedDocumentSupport -> "YesNoExtendedDataPointFormField"
                else -> throw IllegalArgumentException("YesNoComponent does not support document support '$documentSupport")
            }
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = uploadComponentNameToUse,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedYesNo()",
                nullableFixtureExpression = "dataGenerator.randomYesNo()",
                nullable = isNullable,
            ),
        )
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "EnumYesNo",
        )
    }
}
