package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
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

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val annotations = getMinMaxDatamodelAnnotations(minimumValue, maximumValue)

        dataClassBuilder.addProperty(
            identifier,
            documentSupport.getJvmTypeReference(
                TypeReference("java.math.BigInteger", isNullable),
                isNullable,
            ),
            annotations,
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val componentName = when (documentSupport) {
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
}
