package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service responsible for managing email subscriptions.
 */
@Component("EmailSubscriptionService")
class EmailSubscriptionService(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
) {
    /**
     * Unsubscribes an email based on the provided UUID.
     *
     * This method sets the `isSubscribed` flag to `false` for the email subscription entity
     * identified by the given UUID. If no entity is found with the provided UUID, the method
     * performs no action.
     *
     * @param uuid The UUID of the email subscription to unsubscribe.
     */
    @Transactional
    fun unsubscribeEmailWithUuid(uuid: UUID) {
        // TODO in this event we want to send an stakeholder email to inform about the subscription
        emailSubscriptionRepository.findByUuid(uuid)?.let {
            it.isSubscribed = false
        }
    }

    /**
     * Checks if the specified email address is subscribed.
     *
     * This function queries the [EmailSubscriptionRepository] to determine whether the
     * provided [emailAddress] is currently subscribed. It returns `true` if the email
     * is subscribed, `false` if it is not subscribed, and `null` if the email address
     * does not exist in the repository.
     */
    @Transactional(readOnly = true)
    fun emailIsSubscribed(emailAddress: String): Boolean? = emailSubscriptionRepository.findByEmailAddress(emailAddress)?.isSubscribed

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
}
