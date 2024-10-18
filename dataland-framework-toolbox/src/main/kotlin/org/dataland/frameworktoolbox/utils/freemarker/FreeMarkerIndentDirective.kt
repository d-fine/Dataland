package org.dataland.frameworktoolbox.utils.freemarker

import freemarker.core.Environment
import freemarker.template.TemplateDirectiveBody
import freemarker.template.TemplateDirectiveModel
import freemarker.template.TemplateModel
import java.io.StringWriter

/**
 * A FreeMarker directive for indenting all contained content equally
 */
class FreeMarkerIndentDirective : TemplateDirectiveModel {
    private val indent = "    "

    override fun execute(
        env: Environment,
        params: MutableMap<Any?, Any?>,
        loopVars: Array<out TemplateModel>,
        body: TemplateDirectiveBody,
    ) {
        val intermediateWriter = StringWriter()
        body.render(intermediateWriter)
        val intermediateOutput = intermediateWriter.toString()

        if (intermediateOutput.isNotBlank()) {
            env.out.write(intermediateOutput.prependIndent(indent))
        }
    }
}
