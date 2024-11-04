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
    fun addSubscription(emailAddress: String): UUID {
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
     * This function queries the email subscription repository to determine whether the
     * provided email address is currently subscribed.
     *
     * @param emailAddress that should be checked
     * @return Boolean which is `true` if the email is subscribed,
     * `false` if it is not subscribed or if the email address
     *  does not exist in the repository.
     */
    fun isEmailSubscribed(emailAddress: String): Boolean =
        emailSubscriptionRepository.findByEmailAddress(emailAddress)?.isSubscribed ?: false

    /**
     * This functions checks whether an email contact should be filtered or not.
     * @param emailContact that should be checked
     * @return Boolean which is 'true' if the contact should be filtered and 'false' otherwise.
     */
    fun shouldSendToEmailContact(emailContact: EmailContact): Boolean =
        !emailContact.emailAddress.contains("@example.com") &&
            isEmailSubscribed(emailContact.emailAddress)
}
