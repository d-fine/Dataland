package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.DataPointMaximumValueAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.DataPointMinimumValueAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
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

    private fun getMinMaxValidationRule(): String? {
        return if (minimumValue != null && maximumValue != null) {
            "between:$minimumValue,$maximumValue"
        } else if (minimumValue != null) {
            "min:$minimumValue"
        } else if (maximumValue != null) {
            "max:$maximumValue"
        } else {
            null
        }
    }

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val annotations = mutableListOf<Annotation>()

        if (documentSupport is SimpleDocumentSupport || documentSupport is ExtendedDocumentSupport) {
            annotations.add(ValidAnnotation)
            minimumValue?.let { annotations.add(DataPointMinimumValueAnnotation(it)) }
            maximumValue?.let { annotations.add(DataPointMaximumValueAnnotation(it)) }
        }

        dataClassBuilder.addProperty(
            identifier,
            documentSupport.getJvmTypeReference(
                TypeReference(fullyQualifiedNameOfKotlinType, isNullable),
                isNullable,
            ),
            annotations,
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val uploadComponent = when (documentSupport) {
            is NoDocumentSupport -> "NumberFormField"
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
            validation = getMinMaxValidationRule(),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val rangeParameterSpecification = if (minimumValue != null && maximumValue != null) {
            "$minimumValue, $maximumValue"
        } else if (minimumValue != null) {
            "$minimumValue"
        } else if (maximumValue != null) {
            "0, $maximumValue"
        } else {
            ""
        }

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
