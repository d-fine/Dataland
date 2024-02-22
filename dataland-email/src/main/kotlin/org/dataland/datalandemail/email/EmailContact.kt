package org.dataland.datalandemail.email

import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.TransactionalEmail
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException

/**
 * A class to represent an email contact
 */
data class EmailContact(
    val emailAddress: String,
    val name: String? = null,
) {
    /**
     * Converts a Dataland EmailContact object into a SendContact object of the mailjet client library
     */
    fun toMailjetSendContact(): SendContact {
        return if (name == null) SendContact(emailAddress) else SendContact(emailAddress, name)
    }
}

/**
 * Integrates the provided EmailContact object as sender into the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateSenderIntoTransactionalEmailBuilder(sender: EmailContact):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.from(sender.toMailjetSendContact())
}

/**
 * Integrates the provided list of EmailContact objects as receivers into the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder
    .integrateReceiversIntoTransactionalEmailBuilder(receivers: List<EmailContact>):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.to(receivers.map { it.toMailjetSendContact() })
}

/**
 * Integrates the provided list of EmailContact objects as cc receivers into the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder
    .integrateCcIntoTransactionalEmailBuilder(ccReceivers: List<EmailContact>):
    TransactionalEmail.TransactionalEmailBuilder {
    return this.cc(ccReceivers.map { it.toMailjetSendContact() })
}

/**
 * Checks if a string is an email address
 * @returns true if and only if the string matches email address pattern
 */
fun String.isEmailAddress() = Regex("^[a-zA-Z0-9_.!-]+@([a-zA-Z0-9-]+.){1,2}[a-z]{2,}\$").matches(this)

/**
 * Checks if a string is an email address
 * @returns true if and only if the string matches email address pattern
 */
fun String.isEmailAddress() = Regex("^[a-zA-Z0-9_.!-]+@([a-zA-Z0-9-]+.){1,2}[a-z]{2,}\$").matches(this)

/**
 * Validates that a string is an email address and throws an exception if not
 * @throws InvalidEmailFormatApiException if the email format is violated
 */
fun String.validateIsEmailAddress() {
    if (!isEmailAddress()) {
        throw InvalidEmailFormatApiException(this)
    }
}

/**
 * An API exception which should be raised if an email format is violated
 */
class InvalidEmailFormatApiException(email: String) : InvalidInputApiException(
    "Invalid email address \"$email\"",
    "The email address \"$email\" you have provided has an invalid format.",
)
