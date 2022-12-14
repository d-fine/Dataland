package org.dataland.datalandbackend.model.email

import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.TransactionalEmail

/**
 * A class to represent the subject, content and attachments of an email
 */
data class EmailContact(
    val email: String,
    val name: String? = null
) {
    fun toSendContact(): SendContact {
        return if (name == null) SendContact(email) else SendContact(email, name)
    }
}

/**
 * Uses a list of EmailContact objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.from(sender: EmailContact):
        TransactionalEmail.TransactionalEmailBuilder {
    return this.from(sender.toSendContact())
}

/**
 * Uses a list of EmailContact objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.to(receivers: List<EmailContact>):
        TransactionalEmail.TransactionalEmailBuilder {
    return this.to(receivers.map { it.toSendContact() })
}

/**
 * Uses a list of EmailContact objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.cc(receivers: List<EmailContact>):
        TransactionalEmail.TransactionalEmailBuilder {
    return this.cc(receivers.map { it.toSendContact() })
}
