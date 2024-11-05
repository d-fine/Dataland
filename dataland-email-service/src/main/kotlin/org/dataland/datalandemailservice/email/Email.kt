package org.dataland.datalandemailservice.email

import com.mailjet.client.transactional.TransactionalEmail

/**
 * --- Non-API model ---
 * A class that stores the sender, receiver and content of an email
 */
data class Email(
    val sender: EmailContact,
    val receivers: List<EmailContact>,
    val cc: List<EmailContact>,
    val bcc: List<EmailContact>,
    val content: EmailContent,
)

/**
 * Uses an Email object for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.integrateEmailIntoTransactionalEmailBuilder(
    email: Email,
): TransactionalEmail.TransactionalEmailBuilder =
    this
        .integrateSenderIntoTransactionalEmailBuilder(email.sender)
        .integrateReceiversIntoTransactionalEmailBuilder(email.receivers)
        .apply {
            email.cc?.let { ccReceivers ->
                this.integrateCcIntoTransactionalEmailBuilder(ccReceivers)
            }
        }.integrateContentIntoTransactionalEmailBuilder(email.content)
