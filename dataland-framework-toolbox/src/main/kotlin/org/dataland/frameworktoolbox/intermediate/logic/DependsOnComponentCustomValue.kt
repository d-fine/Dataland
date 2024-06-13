package org.dataland.frameworktoolbox.intermediate.logic

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * A DependsOnComponentValue is a FrameworkConditional that is true
 * iff the specified component has the value given by the value property
 */
class DependsOnComponentCustomValue(var component: ComponentBase, var value: String, var component2: ComponentBase?) : FrameworkConditional() {
    override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda {
        val operator = value.substring(0, 2)
        return if (component2 != null) {
            FrameworkBooleanLambda(
                "(parseInt(${component.getTypescriptFieldAccessor(true)} " +
                    "?? ${component2!!.getTypescriptFieldAccessor(true)})) " +
                    "$operator ${StringEscapeUtils.escapeEcmaScript(value.substring(2))}",
            )
        } else {
            FrameworkBooleanLambda(
                "parseInt(${component.getTypescriptFieldAccessor(true)}) " +
                    "$operator ${StringEscapeUtils.escapeEcmaScript(value.substring(2))}",
            )
        }
    }
    // TODO Vergleichbare neue Funktion anlegen, die den Fall für > handled. Könnte DependsOnCompnentValueNumeric heißen
    // TODO Gegebenenfalls muss für den numerischen Wert ein parseInt verwendet werden
}
