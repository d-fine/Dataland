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
        /**
         * Builds the EmailContent from a subject and freemarker templates.
         * @param subject The subject of the email.
         * @param templateContext The context used in the content creation process for the text and html template.
         * @param textTemplate The name of the text template used to create the text content.
         * @param htmlTemplate The name of the html template used to create the html content.
         */
        fun fromTemplates(
            subject: String,
            templateContext: Any,
            textTemplate: String,
            htmlTemplate: String,
        ): EmailContent =
            EmailContent(
                subject,
                buildTemplate(templateContext, textTemplate),
                buildTemplate(templateContext, htmlTemplate),
            )

        private fun buildTemplate(
            templateContext: Any,
            templateName: String,
        ): String {
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
