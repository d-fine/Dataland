package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ElementaryEventRepository : JpaRepository<ElementaryEventEntity, String> {

    // TODO Test
    fun findAllByCompanyIdAndNotificationEventIsNull(companyId: String): List<ElementaryEventEntity>
}
