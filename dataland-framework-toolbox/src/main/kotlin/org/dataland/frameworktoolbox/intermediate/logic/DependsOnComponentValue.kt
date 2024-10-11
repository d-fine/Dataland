package org.dataland.frameworktoolbox.intermediate.logic

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * A DependsOnComponentValue is a FrameworkConditional that is true
 * iff the specified component has the value given by the value property
 */
class DependsOnComponentValue(
    var component: ComponentBase,
    var value: String,
) : FrameworkConditional() {
    override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda =
        FrameworkBooleanLambda(
            "${component.getTypescriptFieldAccessor(true)} " +
                "== \"${StringEscapeUtils.escapeEcmaScript(value)}\"",
        )
}
