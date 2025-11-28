package org.dataland.datalanduserservice.repository

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA repository for storing and retrieving notification events
 */
interface NotificationEventRepository : JpaRepository<NotificationEventEntity, UUID> {
    /**
     * Returns all NotificationEventEntities matching framework, list of companyIds, and newer than given timestamp.
     */
    fun findAllByFrameworkAndCompanyIdInAndCreationTimestampGreaterThan(
        framework: DataTypeEnum,
        companyIds: List<UUID>,
        creationTimestamp: Long,
    ): List<NotificationEventEntity>
}
