package org.dataland.frameworktoolbox.frameworks.sme.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * A DependsOnComponentValue is a FrameworkConditional that is true
 * iff the specified component has the value given by the value property
 */
class DependsOnComponentCustomValue(var component: ComponentBase, var value: String, var component2: ComponentBase) :
    FrameworkConditional() {
    override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda {
        // TODO Emanuel: Das Ding wird eh nur mit ">=150" genutzt oder? Dann lass die hardcodet dafür bauen vllt?
        val operator = value.substring(0, 2)
        return FrameworkBooleanLambda(
            "{\n" +
                "const firstValue = ${component.getTypescriptFieldAccessor(true)} ?? 0;\n" +
                "const secondValue = ${component2.getTypescriptFieldAccessor(true)} ?? 0;\n" +
                "return firstValue $operator ${StringEscapeUtils.escapeEcmaScript(value.substring(2))} " +
                "|| " +
                "secondValue $operator ${StringEscapeUtils.escapeEcmaScript(value.substring(2))}\n" +
                "}",
        )
    }
    // TODO Vergleichbare neue Funktion anlegen, die den Fall für > handled. Könnte DependsOnCompnentValueNumeric heißen
    // TODO Gegebenenfalls muss für den numerischen Wert ein parseInt verwendet werden

    /* TODO Emanuel: Da das hier ja eine custom Implementierung spezifisch für SME ist, sollte es nicht im"intermediate"
         Ordner liegen. Daher habe ich es in den "custom" Ordner von SME verschoben. Passt? @Stephan, Marcel?
    ( Klar könnte man das hier einfach als "allgemeine" Erweiterung der toolbox handlen, aber dafür ist das meiner
    Meinung nach viel zu spezifisch. Das Ding hier funktioniert ja eigentlich nur für diesen einen Fall in vSme.)
     */
}
