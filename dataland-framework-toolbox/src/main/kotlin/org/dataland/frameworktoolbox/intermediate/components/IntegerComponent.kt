package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder

/**
 * A IntegerComponent represents a numeric value from the integer domain
 */
open class IntegerComponent(
    identifier: String,
    parent: FieldNodeParent,
) : NumberBaseComponent(identifier, parent) {
    var minimumValue: Long? = null
    var maximumValue: Long? = null

    val example = """ {
    "value" : 100,
    "quality" : "Reported",
    "comment" : "The value is reported by the company.",
    "dataSource" : {
      "page" : "5-7",
      "tagName" : "monetaryAmount",
      "fileName" : "AnnualReport2020.pdf",
      "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    }"""

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val annotations = getAnnotationsWithMinMax(example, minimumValue, maximumValue)

        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference("java.math.BigInteger", isNullable),
            annotations,
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                "java.math.BigInteger",
                isNullable,
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val componentName =
            when (documentSupport) {
                is NoDocumentSupport -> "NumberFormField"
                is ExtendedDocumentSupport -> "BigDecimalExtendedDataPointFormField"
                else ->
                    throw IllegalArgumentException("IntegerComponent does not support document support '$documentSupport")
            }
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = componentName,
            validation = "integer${getMinMaxValidationRule(minimumValue, maximumValue)?.let { "|$it" } ?: ""}",
            unit = constantUnitSuffix,
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val rangeParameterSpecification = getFakeFixtureMinMaxRangeParameterSpec(minimumValue, maximumValue)
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedInt($rangeParameterSpecification)",
                nullableFixtureExpression = "dataGenerator.randomInt($rangeParameterSpecification)",
                nullable = isNullable,
            ),
        )
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "Integer",
        )
    }

    override fun getConstraints(): List<String>? = getMinMaxValidationRule(minimumValue, maximumValue)?.let { listOf(it) }
}
