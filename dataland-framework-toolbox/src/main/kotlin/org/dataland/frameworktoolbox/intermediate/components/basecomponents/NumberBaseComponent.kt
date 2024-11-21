package org.dataland.frameworktoolbox.intermediate.components.basecomponents

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.MaximumValueAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.annotations.MinimumValueAnnotation
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * The NumberBaseComponent serves as base-class for any number component (percentage, decimal, integer...) and
 * offers some standard functionality.
 */
open class NumberBaseComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    open var constantUnitSuffix: String? = null

    /**
     * Returns a FormKit validation rule for a number to be in the specified range
     */
    fun getMinMaxValidationRule(
        minimumValue: Long?,
        maximumValue: Long?,
    ): String? =
        when {
            minimumValue != null && maximumValue != null -> {
                "between:$minimumValue,$maximumValue"
            }
            minimumValue != null -> {
                "min:$minimumValue"
            }
            maximumValue != null -> {
                "max:$maximumValue"
            }
            else -> {
                null
            }
        }

    /**
     * Returns a list of datamodel annotations to enforce the minimum and maximum value constraints
     */
    fun getMinMaxDatamodelAnnotations(
        minimumValue: Long?,
        maximumValue: Long?,
    ): List<Annotation> {
        val annotations = mutableListOf<Annotation>()

        if (minimumValue != null || maximumValue != null) {
            minimumValue?.let { annotations.add(MinimumValueAnnotation(it)) }
            maximumValue?.let { annotations.add(MaximumValueAnnotation(it)) }
        }
        return annotations
    }

    /**
     * Returns the parameter list for the fake fixture generation to respect minimum and maximum bounds
     */
    fun getFakeFixtureMinMaxRangeParameterSpec(
        minimumValue: Long?,
        maximumValue: Long?,
    ): String =
        when {
            minimumValue != null && maximumValue != null -> {
                "$minimumValue, $maximumValue"
            }
            minimumValue != null -> {
                "$minimumValue"
            }
            maximumValue != null -> {
                "0, $maximumValue"
            }
            else -> {
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
                        TypeScriptImport(
                            "formatNumberForDatatable",
                            "@/components/resources/dataTable/conversion/NumberValueGetterFactory",
                        ),
                    ),
                ),
                label, getTypescriptFieldAccessor(),
            ),
        )
    }
}
