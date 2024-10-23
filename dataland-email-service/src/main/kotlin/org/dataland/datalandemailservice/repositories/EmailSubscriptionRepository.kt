package org.dataland.datalandemailservice.repositories

import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Repository interface for accessing and managing EmailSubscriptionEntity data.
 */
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
