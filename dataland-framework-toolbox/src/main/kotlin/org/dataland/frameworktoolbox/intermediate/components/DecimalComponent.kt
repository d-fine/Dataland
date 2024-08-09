package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder

/**
 * A DecimalComponent represents a numeric decimal value
 */
open class DecimalComponent(
    identifier: String,
    parent: FieldNodeParent,
) : NumberBaseComponent(identifier, parent) {

    var minimumValue: Long? = null
    var maximumValue: Long? = null
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val annotations = getMinMaxDatamodelAnnotations(minimumValue, maximumValue)

        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                "java.math.BigDecimal",
                isNullable,
            ),
            annotations,
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                "java.math.BigDecimal",
                isNullable,
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val uploadComponent = when (documentSupport) {
            is NoDocumentSupport -> "NumberFormField"
            is SimpleDocumentSupport -> "BigDecimalBaseDataPointFormField"
            is ExtendedDocumentSupport -> "BigDecimalExtendedDataPointFormField"
            else -> throw IllegalArgumentException(
                "Upload-page generation for this component " +
                    "does not support $documentSupport",
            )
        }

        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = uploadComponent,
            unit = constantUnitSuffix,
            validation = getMinMaxValidationRule(minimumValue, maximumValue),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val rangeParameterSpecification = getFakeFixtureMinMaxRangeParameterSpec(minimumValue, maximumValue)

        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedFloat($rangeParameterSpecification)",
                nullableFixtureExpression = "dataGenerator.randomFloat($rangeParameterSpecification)",
                nullable = isNullable,
            ),
        )
    }
}
