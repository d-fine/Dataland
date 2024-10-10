package org.dataland.frameworktoolbox.utils.freemarker

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.util.TimeZone

/**
 * Configures a global FreeMarker instance for generating code
 */
object FreeMarker {
    val configuration: Configuration by lazy {
        val cfg = Configuration(Configuration.VERSION_2_3_32)
        cfg.setClassForTemplateLoading(FreeMarker::class.java, "/org/dataland/frameworktoolbox")
        cfg.setSharedVariable("indent", FreeMarkerIndentDirective())
        cfg.defaultEncoding = "UTF-8"
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        cfg.logTemplateExceptions = false
        cfg.wrapUncheckedExceptions = true
        cfg.fallbackOnNullLoopVariable = false
        cfg.sqlDateAndTimeTimeZone = TimeZone.getDefault()
        cfg
    }
}
