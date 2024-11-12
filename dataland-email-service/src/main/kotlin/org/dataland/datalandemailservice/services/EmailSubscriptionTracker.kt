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
 * Note, that this service intentionally only works with EmailContact instead of plain email addresses to.
 */
@Service
class EmailSubscriptionTracker(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
) {
    /**
     * This functions checks if there is already and entity for an email address.
     * If there is entity, this entity is returned.
     * If there is no entity yet, an entity is created, saved, and then returned.
     * Important: This function should be called inside a Transactional block. Do not use this function outside of
     * this Service.
     */
    fun getOrAddSubscription(emailAddress: String): EmailSubscriptionEntity =
        emailSubscriptionRepository.findByEmailAddress(emailAddress)
            ?: emailSubscriptionRepository.save(
                EmailSubscriptionEntity(
                    emailAddress = emailAddress,
                    isSubscribed = true,
                ),
            )

    /**
     * A class that stores the return values of the [subscribeContactsIfNeededAndPartition] method.
     * The class stores a partition of the contacts into allowed contacts, i.e. contacts that should receive an email, and
     * blocked contacts, i.e. contacts that should not receive an email.
     * Every allowed contact is associated with a subscription uuid, that can be used to unsubscribe the receiver.
     */
    data class PartitionedContacts(
        val allowedContacts: Map<EmailContact, UUID>,
        val blockedContacts: List<EmailContact>,
    )

    /**
     * This function does two things:
     * First, it ensures that every contact in [contacts] has an associated entity in the repository.
     * If there is no entity, an entity is created with [EmailSubscriptionEntity.isSubscribed] set to true.
     * Second, the function partitions the contacts into allowed contacts and blocked contacts.
     * @param contacts The list of [EmailContact] that should be processed.
     * @return An instance of [PartitionedContacts] that stores the allowed contacts and the blocked contacts.
     */
    @Transactional
    fun subscribeContactsIfNeededAndPartition(contacts: List<EmailContact>): PartitionedContacts {
        val (allowedEntities, blockedEntities) =
            contacts
                .map { it to getOrAddSubscription(it.emailAddress) }
                .partition { (_, entity) -> entity.shouldReceiveEmail() }

        val allowedContactsToSubscriptionUuid =
            allowedEntities
                .associate { (contact, entity) -> contact to entity.uuid }

        val blockedContacts =
            blockedEntities
                .map { (contact, _) -> contact }

        return PartitionedContacts(
            allowedContactsToSubscriptionUuid,
            blockedContacts,
        )
    }

    /**
     * This function queries the email subscription repository for a contact email address and checks the subscription status.
     * If there is no entity with this email, an entity is created with [EmailSubscriptionEntity.isSubscribed] set to true.
     * The function returns the subscription status of the entity.
     * @param contact that should be checked
     * @return `true` if the email should be sent and `false` otherwise.
     */
    @Transactional
    fun shouldReceiveEmail(contact: EmailContact): Boolean = getOrAddSubscription(contact.emailAddress).shouldReceiveEmail()
}
