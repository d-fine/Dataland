package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Class responsible for sending unsubscription information to stakeholders.
 */
@Component
class UnsubscriptionService(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val internalEmailBuilder: InternalEmailBuilder,
    @Autowired private val emailSubscriptionService: EmailSubscriptionService,
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unsubscribes an email address from email communications based on the provided UUID and sends a mail
     * to the stakeholders that this email address is now unsubscribed.
     * @param uuid The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    fun unsubscribeUuidAndSendMailToStakeholders(uuid: UUID): ResponseEntity<String> {
        val emailSubscription = emailSubscriptionRepository.findByUuid(uuid)

        if (emailSubscription != null) {
            emailSubscriptionService.unsubscribeEmailWithUuid(emailSubscription.uuid)
            sendUnsubscriptionEmail(emailSubscription.emailAddress)

            logger.info("Successfully unsubscribed email address corresponding to UUID: $uuid")
            return ResponseEntity.ok("Successfully unsubscribed email address corresponding to the UUID: $uuid.")
        } else {
            logger.info("There is no email address corresponding to the UUID: $uuid.")
            return ResponseEntity.ok("There is no email address corresponding to the UUID: $uuid.")
        }
    }

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
                properties = mapOf("Unsubscribed Email Address" to unsubscribedEmailAddress),
            )
        emailSender.sendEmail(internalEmailBuilder.buildInternalEmail(unsubscriptionMessage))
    }
}
