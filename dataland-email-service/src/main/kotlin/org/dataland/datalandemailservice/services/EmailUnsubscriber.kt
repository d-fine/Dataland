package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service responsible for managing email subscriptions.
 */
@Component
class EmailUnsubscriber(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
    @Autowired private val emailSender: EmailSender,
    @Autowired private val internalEmailBuilder: InternalEmailBuilder,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unsubscribes an email address from email communications based on the provided UUID and sends a mail
     * to the stakeholders that this email address is now unsubscribed.
     * @param uuid The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    @Transactional
    fun unsubscribeUuidAndSendMailToStakeholders(uuid: UUID): ResponseEntity<String> {
        val emailSubscription = emailSubscriptionRepository.findByUuid(uuid)

        return if (emailSubscription != null) {
            unsubscribeEmailWithUuid(emailSubscription.uuid)
            sendUnsubscriptionEmail(emailSubscription.emailAddress)

            logger.info("Successfully unsubscribed email address corresponding to UUID: $uuid")
            ResponseEntity.ok("Successfully unsubscribed email address corresponding to the UUID: $uuid.")
        } else {
            logger.info("There is no email address corresponding to the UUID: $uuid.")
            ResponseEntity.ok("There is no email address corresponding to the UUID: $uuid.")
        }
    }

    /**
     * Unsubscribes an email based on the provided UUID.
     *
     * This method sets the `isSubscribed` flag to `false` for the email subscription entity
     * identified by the given UUID. If no entity is found with the provided UUID, the method
     * performs no action.
     *
     * @param uuid The UUID of the email subscription to unsubscribe.
     */
    private fun unsubscribeEmailWithUuid(uuid: UUID) {
        emailSubscriptionRepository.findByUuid(uuid)?.let {
            it.isSubscribed = false
        }
    }

    /**
     * Send a mail with the information who unsubscribed to the stakeholders.
     *
     * @param unsubscribedEmailAddress The email address of the person who unsubscribed.
     */
    private fun sendUnsubscriptionEmail(unsubscribedEmailAddress: String) {
        val unsubscriptionMessage =
            InternalEmailMessage(
                subject = "Someone has unsubscribed from notifications of data uploads",
                textTitle = "$unsubscribedEmailAddress has unsubscribed from notifications of data uploads.",
                htmlTitle = "$unsubscribedEmailAddress has unsubscribed from notifications of data uploads.",
                properties = mapOf("Unsubscribed Email Address" to unsubscribedEmailAddress),
            )
        emailSender.filterReceiversAndSentEmail(internalEmailBuilder.buildInternalEmail(unsubscriptionMessage))
    }
}
