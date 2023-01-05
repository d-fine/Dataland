package org.dataland.datalandbackend.model.email

import com.mailjet.client.transactional.TransactionalEmail

/**
 * A class to represent the subject, content and attachment of an email
 */
data class EmailContent(
    val subject: String,
    val textContent: String,
    val htmlContent: String,
    val attachments: List<EmailAttachment>
)

/**
 * Uses a Content object for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.content(content: EmailContent):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.subject(content.subject)
        .textPart(content.textContent)
        .htmlPart(content.htmlContent)
        .attachments(content.attachments)
}
