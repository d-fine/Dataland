package org.dataland.datalandemailservice.repositories

import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository interface for accessing and managing EmailSubscriptionEntity data.
 * Every email address that is passed to the email-service is saved in the repository and should have an entity.
 * Per default new entities have an active subscription status, i.e. are subscribed.
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
