package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service responsible for managing email subscriptions.
 */
@Service
class EmailSubscriptionTracker(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
) {
    /**
     * Inserts a new email subscription if one does not already exist and returns its UUID.
     *
     * First, this method checks if an email subscription exists for the given email address.
     * If it does not exist, a new subscription is created with `isSubscribed` set to `true`.
     * Second, the method returns the UUID of the subscription entity for the email address.
     *
     * @param emailAddress The email address to subscribe.
     * @return The UUID of the active/inactive subscription,
     * or the UUID of the newly created entity if no subscription existed.
     */
    @Transactional
    fun addSubscriptionIfNeededAndReturnUuid(emailAddress: String): UUID {
        val entity =
            emailSubscriptionRepository.findByEmailAddress(emailAddress)
                ?: emailSubscriptionRepository.save(
                    EmailSubscriptionEntity(
                        emailAddress = emailAddress,
                        isSubscribed = true,
                    ),
                )

        return entity.uuid
    }

    /**
     * This function queries the email subscription repository for the email address and checks the subscription status.
     * If there is an entity, the value of isSubscribed is returned. Otherwise, true is returned.
     * Since the repository acts as a blacklist, no subscription entity for the email address indicates that the email should be sent.
     *
     * @param emailAddress that should be checked
     * @return `true` if the email is subscribed or no entity is found, false otherwise.
     */
    fun isEmailSubscribed(emailAddress: String): Boolean =
        emailSubscriptionRepository.findByEmailAddress(emailAddress)?.isSubscribed ?: true

    /**
     * This function checks whether an email should be sent to an email contact.
     * The email should be sent if the email address is subscribed and the email does not have the @example.com domain.
     *
     * @param emailContact that should be checked
     * @return Boolean which is 'true' if the contact should be filtered and 'false' otherwise.
     */
    fun shouldSendToEmailContact(emailContact: EmailContact): Boolean =
        !emailContact.emailAddress.contains("@example.com") &&
            isEmailSubscribed(emailContact.emailAddress)
}
