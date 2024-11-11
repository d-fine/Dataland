package org.dataland.datalandemailservice.email

import com.mailjet.client.transactional.TransactionalEmail
import java.io.StringWriter

/**
 * A class to represent the subject and content of an email
 */
data class EmailContent(
    val subject: String,
    val textContent: String,
    val htmlContent: String,
) {
    companion object {
        fun fromTemplates(subject: String, templateContext: Any, textTemplate: String, htmlTemplate: String): EmailContent =
            EmailContent(
                subject,
                buildTemplate(templateContext, textTemplate),
                buildTemplate(templateContext, htmlTemplate)
            )

        private fun buildTemplate(templateContext: Any, templateName: String): String {
            val freemarkerTemplate = FreeMarker.configuration.getTemplate(templateName)

            val writer = StringWriter()
            freemarkerTemplate.process(templateContext, writer)
            writer.close()
            return writer.toString()
        }
    }
}

/**
 * Uses a Content object for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateContentIntoTransactionalEmailBuilder(
    content: EmailContent,
): TransactionalEmail.TransactionalEmailBuilder =
    this
        .subject(content.subject)
        .textPart(content.textContent)
        .htmlPart(content.htmlContent)
