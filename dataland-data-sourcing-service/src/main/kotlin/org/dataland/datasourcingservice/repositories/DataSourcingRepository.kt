package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * A JPA Repository for managing RequestEntity instances.
 */
interface DataSourcingRepository : JpaRepository<DataSourcingEntity, UUID> {
    /**
     * Find a DataSourcingEntity by the triple companyId, dataType and reportingPeriod.
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriod(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): DataSourcingEntity?
}
