package org.dataland.datalandemailservice.email

import com.mailjet.client.transactional.TransactionalEmail

/**
 * A class to represent the subject and content of an email
 */
data class EmailContent(
    val subject: String,
    val textContent: String,
    val htmlContent: String,
)

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
