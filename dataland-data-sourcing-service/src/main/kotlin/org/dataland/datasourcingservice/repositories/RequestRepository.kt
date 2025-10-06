package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.RequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

/**
 * A JPA Repository for managing RequestEntity instances.
 */
interface RequestRepository : JpaRepository<RequestEntity, UUID> {
    /**
     * Find a request by the quadruple userId, companyId, dataType and reportingPeriod.
     */
    fun findByUserIdAndCompanyIdAndDataTypeAndReportingPeriod(
        userId: UUID,
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
    ): List<RequestEntity>

    /**
     * Find a request by id and fetch the associated data sourcing entity.
     */
    @Query(
        "SELECT request FROM RequestEntity request " +
            "LEFT JOIN FETCH request.dataSourcingEntity " +
            "WHERE request.id = :id",
    )
    fun findByIdAndFetchDataSourcingEntity(id: UUID): RequestEntity?
}
