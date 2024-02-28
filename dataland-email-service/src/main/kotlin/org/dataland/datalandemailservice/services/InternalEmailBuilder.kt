package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.PropertyStyleEmailBuilder
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that creates mail for internal use
 */
@Component
class InternalEmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Value("\${dataland.notification.receivers}") semicolonSeparatedReceiverEmails: String,
    @Value("\${dataland.notification.cc}") semicolonSeparatedCcEmails: String,
) : PropertyStyleEmailBuilder(
    senderEmail = senderEmail,
    senderName = senderName,
    semicolonSeparatedReceiverEmails = semicolonSeparatedReceiverEmails,
    semicolonSeparatedCcEmails = semicolonSeparatedCcEmails,
) {
    /**
     * Function that generates internal emails
     */
    fun buildInternalEmail(
        internalEmailMessage: InternalEmailMessage,
    ): Email {
        return buildPropertyStyleEmail(
            subject = internalEmailMessage.subject,
            textTitle = internalEmailMessage.textTitle,
            properties = internalEmailMessage.properties + mapOf("Environment" to proxyPrimaryUrl),
            htmlTitle = internalEmailMessage.htmlTitle + "blablabla",
        )
    }
}
