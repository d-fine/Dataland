package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA repository for storing and retrieving elementary events
 */
interface ElementaryEventRepository : JpaRepository<ElementaryEventEntity, UUID> {

    /**
     * A function for searching for elementary events by companyId and the type of the elementary events
     * @param companyId to filter for
     * @param elementaryEventType to filter for
     * @returns the elementary events
     */
    fun findAllByCompanyIdAndElementaryEventTypeAndNotificationEventIsNull(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): List<ElementaryEventEntity>
}
