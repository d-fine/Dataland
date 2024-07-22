package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ElementaryEventRepository : JpaRepository<ElementaryEventEntity, UUID> {

    // TODO Test
    fun findAllByCompanyIdAndElementaryEventTypeAndNotificationEventIsNull(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): List<ElementaryEventEntity>
}
