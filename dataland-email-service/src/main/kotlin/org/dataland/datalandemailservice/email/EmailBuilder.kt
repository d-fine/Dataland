package org.dataland.datalandemailservice.email

import java.io.StringWriter

/**
 * TODO
 */
data class EmailBuilder(
    val templateContext: Any,
    val subject: String,
    val htmlTemplateFile: String,
    val textTemplateFile: String,
) {

    /**
     * TODO
     */
    fun build(sender: EmailContact, receivers: List<EmailContact>, cc: List<EmailContact>, bcc: List<EmailContact>): Email =
        Email(sender, receivers, cc, bcc, buildEmailContent())

    /**
     * TODO
     */
    private fun buildEmailContent() =
        EmailContent(
            subject, buildTemplate(textTemplateFile), buildTemplate(htmlTemplateFile),
        )

    /**
     * TODO
     */
    private fun buildTemplate(file: String) : String {
        val freemarkerTemplate = FreeMarker.configuration.getTemplate(file)

        val writer = StringWriter()
        freemarkerTemplate.process(templateContext, writer)
        writer.close()
        return writer.toString()
    }
}