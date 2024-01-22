package org.dataland.frameworktoolbox.intermediate.components.basecomponents

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.DataPointMaximumValueAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.DataPointMinimumValueAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * The NumberBaseComponent serves as base-class for any number component (percentage, decimal, integer...) and
 * offers some standard functionality.
 */
open class NumberBaseComponent(
    identifier: String,
    parent: FieldNodeParent,
    fullyQualifiedNameOfKotlinType: String = "java.math.BigDecimal",
) : ComponentBase(identifier, parent, fullyQualifiedNameOfKotlinType) {
    open var constantUnitSuffix: String? = null

    /**
     * Returns a FormKit validation rule for a number to be in the specified range
     */
    fun getMinMaxValidationRule(minimumValue: Long?, maximumValue: Long?): String? {
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

    /**
     * Returns a list of datamodel annotations to enforce the minimum and maximum value constraints
     */
    fun getMinMaxDatamodelAnnotations(minimumValue: Long?, maximumValue: Long?): List<Annotation> {
        val annotations = mutableListOf<Annotation>()

        if (minimumValue != null || maximumValue != null) {
            require(documentSupport is SimpleDocumentSupport || documentSupport is ExtendedDocumentSupport) { "There are currently no minimum/maximum value constraint annotation for non-datapoint fields." }

            annotations.add(ValidAnnotation)
            minimumValue?.let { annotations.add(DataPointMinimumValueAnnotation(it)) }
            maximumValue?.let { annotations.add(DataPointMaximumValueAnnotation(it)) }
        }
        return annotations
    }

    /**
     * Returns the parameter list for the fake fixture generation to respect minimum and maximum bounds
     */
    fun getFakeFixtureMinMaxRangeParameterSpec(minimumValue: Long?, maximumValue: Long?): String {
        return if (minimumValue != null && maximumValue != null) {
            "$minimumValue, $maximumValue"
        } else if (minimumValue != null) {
            "$minimumValue"
        } else if (maximumValue != null) {
            "0, $maximumValue"
        } else {
            ""
        }
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            documentSupport.getFrameworkDisplayValueLambda(
                FrameworkDisplayValueLambda(
                    "formatNumberForDatatable(${getTypescriptFieldAccessor(true)}," +
                        " \"${StringEscapeUtils.escapeEcmaScript(constantUnitSuffix ?: "")}\")",
                    setOf(
                        "import { formatNumberForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/NumberValueGetterFactory\";",
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }
}
