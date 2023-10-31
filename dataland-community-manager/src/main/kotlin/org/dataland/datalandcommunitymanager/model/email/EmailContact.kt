package org.dataland.datalandcommunitymanager.model.email

import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.TransactionalEmail

/**
 * A class to represent an email contact
 */
data class EmailContact(
    val emailAddress: String,
    val name: String? = null,
) {
    /**
     * Defines whether to hide the requester's name in the email or to show it
     */
    fun toMailjetSendContact(): SendContact {
        return if (name == null) SendContact(emailAddress) else SendContact(emailAddress, name)
    }
}

/**
 * Uses a list of EmailContact objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateSenderIntoTransactionalEmailBuilder(sender: EmailContact):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.from(sender.toMailjetSendContact())
}

/**
 * Uses a list of EmailContact objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder
    .integrateReceiversIntoTransactionalEmailBuilder(receivers: List<EmailContact>):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.to(receivers.map { it.toMailjetSendContact() })
}

/**
 * Uses a list of EmailContact objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder
    .integrateCcIntoTransactionalEmailBuilder(receivers: List<EmailContact>):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.cc(receivers.map { it.toMailjetSendContact() })
}
