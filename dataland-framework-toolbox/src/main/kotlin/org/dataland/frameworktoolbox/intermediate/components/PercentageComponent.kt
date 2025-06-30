package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.SuppressKtlintMaxLineLengthAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

private const val MIN_PERCENTAGE: Long = 0
private const val MAX_PERCENTAGE: Long = 100

/**
 * A PercentageComponent represents a decimal percentage between 0 % and 100 %.
 */
class PercentageComponent(
    identifier: String,
    parent: FieldNodeParent,
) : NumberBaseComponent(identifier, parent) {
    override var constantUnitSuffix: String? = "%"
    val example = """{
						"value": 13.52,
						"dataSource": {
							"page": "108",
							"fileName": "AnnualReport",
							"fileReference": "2e7270a3e823927740609089091805c1cedd3cec291175a5ca08c24a8a4b1342",
							"tagName": "supply-chains",
							"publicationDate": null
						},"""

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val schemaAnnotation =
            Annotation(
                fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                rawParameterSpec =
                    "description = \"\"\"${uploadPageExplanation}\"\"\", \n" +
                        "example = \"\"\"$example \"\"\"",
                applicationTargetPrefix = "field",
            )

        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference("java.math.BigDecimal", isNullable),
            listOf(SuppressKtlintMaxLineLengthAnnotation, schemaAnnotation),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference("java.math.BigDecimal", isNullable),
            listOf(),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatPercentageForDatatable(${getTypescriptFieldAccessor(true)})",
                    setOf(
                        TypeScriptImport(
                            "formatPercentageForDatatable",
                            "@/components/resources/dataTable/conversion/PercentageValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport, ExtendedDocumentSupport))
        val uploadComponent =
            when (documentSupport) {
                is NoDocumentSupport -> "NumberFormField"
                is ExtendedDocumentSupport -> "PercentageExtendedDataPointFormField"
                else -> throw IllegalArgumentException(
                    "Upload-page generation for this component " +
                        "does not support $documentSupport",
                )
            }

        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = uploadComponent,
            unit = constantUnitSuffix,
            validation = getMinMaxValidationRule(MIN_PERCENTAGE, MAX_PERCENTAGE),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedPercentageValue()",
                nullableFixtureExpression = "dataGenerator.randomPercentageValue()",
                nullable = isNullable,
            ),
        )
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "Decimal",
        )
    }

    override fun getConstraints(): List<String>? = getMinMaxValidationRule(MIN_PERCENTAGE, MAX_PERCENTAGE)?.let { listOf(it) }
}
