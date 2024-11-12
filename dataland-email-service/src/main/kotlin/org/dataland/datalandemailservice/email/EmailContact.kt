package org.dataland.datalandemailservice.email

import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.TransactionalEmail

/**
 * A class to represent an email contact that is used inside the email-service.
 * Do not use the primary constructor, use the [EmailContact.create] function.
 * This function converts the email address to a lowercase format.
 */
data class EmailContact(
    val emailAddress: String,
    val name: String? = null,
) {
    init {
        require(emailAddress == emailAddress.lowercase()) {
            "Email address must be in lowercase. Provided: '$emailAddress'"
        }
    }

    companion object {
        /**
         * This function creates a [EmailContact] object and by converting the emailAddress into a lowercase format
         * automatically ensures that the created [EmailContact] fulfills the requirements of the data class.
         * @param emailAddress the emailAddress of the contact
         * @param name the optional name of the contact
         * @return the new [EmailContact] instance
         */
        fun create(
            emailAddress: String,
            name: String? = null,
        ): EmailContact = EmailContact(emailAddress.lowercase(), name)
    }

    /**
     * Converts a Dataland EmailContact object into a SendContact object of the mailjet client library
     */
    fun toMailjetSendContact(): SendContact = if (name == null) SendContact(emailAddress) else SendContact(emailAddress, name)
}

/**
 * Integrates the provided EmailContact object as sender into the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateSenderIntoTransactionalEmailBuilder(
    sender: EmailContact,
): TransactionalEmail.TransactionalEmailBuilder = this.from(sender.toMailjetSendContact())

/**
 * Integrates the provided list of EmailContact objects as receivers into the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateReceiversIntoTransactionalEmailBuilder(
    receivers: List<EmailContact>,
): TransactionalEmail.TransactionalEmailBuilder = this.to(receivers.map { it.toMailjetSendContact() })

/**
 * Integrates the provided list of EmailContact objects as cc receivers into the build of a TransactionalEmail
 * Note that incorporating an empty cc list into the TransactionalEmailBuilder is completely fine.
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateCcIntoTransactionalEmailBuilder(
    ccReceivers: List<EmailContact>,
): TransactionalEmail.TransactionalEmailBuilder = this.cc(ccReceivers.map { it.toMailjetSendContact() })

/**
 * Integrates the provided list of EmailContact objects as bcc receivers into the build of a TransactionalEmail
 * Note that incorporating an empty bcc list into the TransactionalEmailBuilder is completely fine.
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateBccIntoTransactionalEmailBuilder(
    bccReceivers: List<EmailContact>,
): TransactionalEmail.TransactionalEmailBuilder = this.bcc(bccReceivers.map { it.toMailjetSendContact() })
