package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
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
@Component("EmailSubscriptionService")
class EmailSubscriptionService(
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
     * Inserts a new email subscription if one does not already exist and returns its UUID.
     *
     * This method checks if an email subscription exists for the given email address.
     * If it does not exist, a new subscription is created with `isSubscribed` set to `true`.
     * The method returns the UUID of the subscription if it is active (subscribed),
     * otherwise returns `null`.
     *
     * @param emailAddress The email address to subscribe.
     * @return The UUID of the active subscription, or `null` if the subscription is inactive.
     */
    @Transactional
    fun insertSubscriptionEntityIfNeededAndReturnUuid(emailAddress: String): UUID {
        val entity =
            emailSubscriptionRepository.findByEmailAddress(emailAddress)
                ?: emailSubscriptionRepository.save(EmailSubscriptionEntity(emailAddress = emailAddress, isSubscribed = true))

        return entity.uuid
    }

    /**
     * Checks if the specified email address is subscribed.
     *
     * This function queries the [EmailSubscriptionRepository] to determine whether the
     * provided [emailAddress] is currently subscribed. It returns `true` if the email
     * is subscribed, `false` if it is not subscribed, and `null` if the email address
     * does not exist in the repository.
     */
    fun emailIsSubscribed(emailAddress: String): Boolean? = emailSubscriptionRepository.findByEmailAddress(emailAddress)?.isSubscribed

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
