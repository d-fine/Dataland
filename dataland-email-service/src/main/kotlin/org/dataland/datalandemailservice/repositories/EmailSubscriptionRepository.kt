package org.dataland.datalandemailservice.repositories

import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository interface for accessing and managing EmailSubscriptionEntity data.
 * Note that this repository represents a blacklist, i.e. only email addresses that have an entity
 * with is_subscribed=false will not receive any other email from the email service.
 */
@Repository
interface EmailSubscriptionRepository : JpaRepository<EmailSubscriptionEntity, UUID> {
    /**
     * Finds an EmailSubscriptionEntity by its email address.
     *
     * @param emailAddress The email address associated with the subscription.
     * @return An EmailSubscriptionEntity if found, or null otherwise.
     */
    fun findByEmailAddress(emailAddress: String): EmailSubscriptionEntity?

    /**
     * Finds an EmailSubscriptionEntity by its UUID.
     *
     * @param uuid The unique identifier of the subscription.
     * @return An EmailSubscriptionEntity if found, or null otherwise.
     */
    fun findByUuid(uuid: UUID): EmailSubscriptionEntity?
}
