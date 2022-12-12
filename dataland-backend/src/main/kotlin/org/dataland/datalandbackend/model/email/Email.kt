package org.dataland.datalandbackend.model.email

import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.TransactionalEmail

/**
 * --- Non-API model ---
 * A class that stores the sender, receiver and content of an email
 */
data class Email(
    val sender: SendContact,
    val receiver: SendContact,
    val content: EmailContent
)

/**
 * Uses an Email object for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.email(email: Email): TransactionalEmail.TransactionalEmailBuilder {
    return this.from(email.sender)
        .to(email.receiver)
        .content(email.content)
}
