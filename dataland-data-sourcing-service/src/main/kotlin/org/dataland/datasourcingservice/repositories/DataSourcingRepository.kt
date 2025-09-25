package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

/**
 * A JPA Repository for managing RequestEntity instances.
 */
interface DataSourcingRepository : JpaRepository<DataSourcingEntity, UUID> {
    /**
     * Find a DataSourcingEntity by the triple companyId, dataType and reportingPeriod.
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriod(
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
    ): DataSourcingEntity?

    /**
     * Used for pre-fetching the associated requests field of a single data sourcing entity
     */
    @Query(
        "SELECT DISTINCT dataSourcing FROM DataSourcingEntity dataSourcing " +
            "LEFT JOIN FETCH dataSourcing.associatedRequests WHERE dataSourcing = :dataSourcingEntity",
    )
    fun fetchAssociatedRequests(dataSourcingEntity: DataSourcingEntity): DataSourcingEntity

    /**
     * Find all DataSourcingEntity instances by companyId of document collector.
     */
    fun findAllByDocumentCollector(companyId: UUID): List<DataSourcingEntity>

    /**
     * Find all DataSourcingEntity instances by companyId of data extractor.
     */
    fun findAllByDataExtractor(companyId: UUID): List<DataSourcingEntity>
}
