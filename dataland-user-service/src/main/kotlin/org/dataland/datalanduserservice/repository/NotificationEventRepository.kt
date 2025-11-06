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
     * * Find all notification events by company ID, framework, reporting period, and creation timestamp greater
     * than the specified value
     */
    fun findAllByCompanyIdAndFrameworkAndReportingPeriodAndCreationTimestampGreaterThan(
        companyId: UUID,
        framework: DataTypeEnum,
        reportingPeriod: String,
        creationTimestamp: Long,
    ): List<NotificationEventEntity>
}
