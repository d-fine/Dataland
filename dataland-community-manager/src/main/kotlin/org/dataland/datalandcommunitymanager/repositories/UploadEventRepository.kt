package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.UploadEventType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA repository for storing and retrieving upload events
 */
interface UploadEventRepository : JpaRepository<ElementaryEventEntity, UUID> {
    /**
     * A function for searching for upload events by companyId and the type of the upload events
     * @param companyId to filter for
     * @param uploadEventType to filter for
     * @returns the upload events
     */
    fun findAllByCompanyIdAndElementaryEventTypeAndNotificationEventIsNull(
        companyId: UUID,
        uploadEventType: UploadEventType,
    ): List<UploadEventEntity>
}
