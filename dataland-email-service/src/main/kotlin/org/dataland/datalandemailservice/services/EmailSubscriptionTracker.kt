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
     * TODO
     * Important: This function should be called inside a Transactional block.
     */
    private fun getOrAddSubscription(emailAddress: String): EmailSubscriptionEntity =
        emailSubscriptionRepository.findByEmailAddress(emailAddress)
            ?: emailSubscriptionRepository.save(
                EmailSubscriptionEntity(
                    emailAddress = emailAddress,
                    isSubscribed = true,
                ),
            )

    data class FilteredContacts(
        val allowed: Map<EmailContact, UUID>,
        val blocked: List<EmailContact>
    )

    /**
     *
     */
    @Transactional
    fun subscribeContactsIfNeededAndFilter(contacts: List<EmailContact>): FilteredContacts {
        val (subscribedEntities, blockedEntities) = contacts
            .map { it to getOrAddSubscription(it.emailAddress) }
            .partition { (_, entity) -> entity.shouldReceiveEmail() }

        val subscribedMap = subscribedEntities
            .associate { (contact, entity) -> contact to entity.uuid }

        val blockedList = blockedEntities
            .map { (contact, _) -> contact }

        return FilteredContacts(
            allowed = subscribedMap,
            blocked = blockedList
        )
    }

    /**
     * This function queries the email subscription repository for the email address and checks the subscription status.
     * If there is an entity, the value of isSubscribed is returned. Otherwise, true is returned.
     * Since the repository acts as a blacklist, no subscription entity for the email address indicates that the email should be sent.
     *
     * @param emailAddress that should be checked
     * @return `true` if the email is subscribed or no entity is found, false otherwise.
     * TODO change logic, such that entity is always created, also add Transactional
     */
    fun isEmailSubscribed(emailAddress: String): Boolean =
        emailSubscriptionRepository.findByEmailAddress(emailAddress)?.isSubscribed ?: true

}
