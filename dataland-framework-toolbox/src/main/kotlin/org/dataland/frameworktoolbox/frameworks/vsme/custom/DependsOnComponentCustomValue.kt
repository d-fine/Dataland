package org.dataland.frameworktoolbox.frameworks.vsme.custom

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * A DependsOnComponentValue is a FrameworkConditional that is true
 * iff the specified component has the value given by the value property
 */
class DependsOnComponentCustomValue(
    var component: ComponentBase,
    var component2: ComponentBase,
) : FrameworkConditional() {
    override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda =
        FrameworkBooleanLambda(
            "{\n" +
                "const firstValue = ${component.getTypescriptFieldAccessor(true)} ?? 0;\n" +
                "const secondValue = ${component2.getTypescriptFieldAccessor(true)} ?? 0;\n" +
                "return firstValue  >=150 || secondValue >=150 }",
        )
}
