package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Class responsible for sending unsubscription information to stakeholders.
 */
@Component
class UnsubscriptionEmailToStakeholdersSender(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val internalEmailBuilder: InternalEmailBuilder,
) {
    /**
     * Send a mail with the information who unsubscribed to the stakeholders.
     *
     * @param unsubscribedEmailAddress The email address of the person who unsubscribed.
     */
    fun sendUnsubscriptionEmail(unsubscribedEmailAddress: String) {
        val unsubscriptionMessage =
            InternalEmailMessage(
                subject = "Someone has unsubscribed from notifications of data uploads",
                textTitle = "$unsubscribedEmailAddress has unsubscribed from notifications of data uploads.",
                htmlTitle = "$unsubscribedEmailAddress has unsubscribed from notifications of data uploads.",
                properties = mapOf("unsubscribedEmailAddress" to unsubscribedEmailAddress),
            )
        emailSender.sendEmail(internalEmailBuilder.buildInternalEmail(unsubscriptionMessage))
    }
}
