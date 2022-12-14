package org.dataland.datalandbackend.model.email

import com.mailjet.client.transactional.TransactionalEmail

/**
 * --- Non-API model ---
 * A class that stores the sender, receiver and content of an email
 */
data class Email(
    val sender: EmailContact,
    val receivers: List<EmailContact>,
    val cc: List<EmailContact>,
    val content: EmailContent
)

/**
 * Uses an Email object for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.email(email: Email): TransactionalEmail.TransactionalEmailBuilder {
    return this.from(email.sender)
        .to(email.receivers)
        .cc(email.cc)
        .content(email.content)
}
