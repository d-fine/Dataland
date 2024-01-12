package org.dataland.frameworktoolbox.intermediate.components.basecomponents

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
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
