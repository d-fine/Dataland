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
     * Find a Request by the triple companyId, dataType and reportingPeriod.
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriod(
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
    ): List<RequestEntity>

    /**
     * Find a request by the quadruple userId, companyId, dataType and reportingPeriod.
     */
    fun findByUserIdAndCompanyIdAndDataTypeAndReportingPeriod(
        userId: UUID,
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
    ): List<RequestEntity>

    /** This method counts the number of data requests that a user
     * has performed from a specified timestamp.
     * @param userId to check for
     * @param timestamp to check for
     * @returns the number of counts
     */
    @Query
    (
        "SELECT COUNT(d.userId) FROM RequestEntity d " +
            "WHERE (d.userId = :#{#userId})" +
            "AND (d.creationTimestamp >= :#{#timestamp})",
    )
    fun getNumberOfDataRequestsPerformedByUserFromTimestamp(
        userId: String,
        timestamp: Long,
    ): Int
}
