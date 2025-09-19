package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.RequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA Repository for managing RequestEntity instances.
 */
interface RequestRepository : JpaRepository<RequestEntity, UUID> {
    /**
     * Find a Request by the triple companyId, dataType and reportingPeriod.
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriod(
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
    ): List<RequestEntity?>
}
