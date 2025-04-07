package org.dataland.datalandemailservice.email

import com.mailjet.client.transactional.TransactionalEmail
import org.slf4j.LoggerFactory // toto: remove
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
        private val logger = LoggerFactory.getLogger(EmailContent::class.java) // toto: remove

        /**
         * Builds the EmailContent from a subject and freemarker templates.
         * @param subject The subject of the email.
         * @param templateContext The context used in the content creation process for the text and html template.
         * @param textTemplate The name of the text template used to create the text content.
         * @param htmlTemplate The name of the html template used to create the html content.
         */
        fun fromTemplates( // toto: remove
            subject: String,
            templateContext: Any,
            textTemplate: String,
            htmlTemplate: String,
        ): EmailContent {
            logger.info("3 Building email content in fromTemplates")
            logger.info("Subject: $subject")
            logger.info("Text Template: $textTemplate")
            logger.info("HTML Template: $htmlTemplate")

            return EmailContent(
                subject,
                buildTemplate(templateContext, textTemplate),
                buildTemplate(templateContext, htmlTemplate),
            )
        }

        private fun buildTemplate(
            templateContext: Any,
            templateName: String,
        ): String {
            logger.info("Building template with name: $templateName") // toto: remove
            val freemarkerTemplate = FreeMarker.configuration.getTemplate(templateName)

            val writer = StringWriter()
            freemarkerTemplate.process(templateContext, writer)
            logger.info("Finished processing template: $templateName") // toto: remove
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
