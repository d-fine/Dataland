package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    /** This method counts the number of data requests that a user
     * has opened after a specified timestamp.
     * @param userId to check for
     * @param timestamp to check for
     * @returns the number of counts
     */
    fun countByUserIdAndCreationTimestampGreaterThanEqual(
        userId: UUID,
        timestamp: Long,
    ): Int

    /**
     * Return the list of all request ids that match the optional filters.
     * @param searchFilter to filter by
     * @param pageable Pageable object
     * @param companyIds list of companyIds matching the company name search string
     * @param userIds list of userIds matching the user email search string
     * @return list of matching request ids
     */
    @Query(
        "SELECT request.id FROM RequestEntity request " +
            "WHERE " +
            "(:#{#searchFilter.companyId} IS NULL OR request.companyId = :#{#searchFilter.companyId}) AND " +
            "((:#{#searchFilter.dataTypes == null} = TRUE) OR request.dataType IN :#{#searchFilter.dataTypes}) AND " +
            "((:#{#companyIds == null} = TRUE) OR request.companyId IN :#{#companyIds}) AND " +
            "((:#{#searchFilter.reportingPeriods == null} = TRUE) OR request.reportingPeriod IN :#{#searchFilter.reportingPeriods}) AND " +
            "(:#{#searchFilter.userId} IS NULL OR request.userId = :#{#searchFilter.userId}) AND " +
            "((:#{#searchFilter.requestStates == null} = TRUE) OR request.state IN :#{#searchFilter.requestStates}) AND " +
            "((:#{#searchFilter.requestPriorities == null} = TRUE) " +
            "OR request.requestPriority IN :#{#searchFilter.requestPriorities}) AND " +
            "(:#{#searchFilter.adminComment} IS NULL " +
            "OR LOWER(request.adminComment) LIKE LOWER(CONCAT('%', :#{#searchFilter.adminComment}, '%'))) AND " +
            "((:#{#userIds == null} = TRUE) OR request.userId IN :#{#userIds})",
    )
    fun searchRequests(
        searchFilter: RequestSearchFilter<UUID>,
        pageable: Pageable,
        companyIds: List<UUID>? = null,
        userIds: List<UUID>? = null,
    ): Page<UUID>

    /**
     * Find all RequestEntity instances by a list of ids and fetch all lazily loaded fields.
     */
    @Query(
        "SELECT request FROM RequestEntity request " +
            "LEFT JOIN FETCH request.dataSourcingEntity " +
            "WHERE " +
            "(request.id IN :requestIds)",
    )
    fun findByListOfIdsAndFetchDataSourcingEntity(requestIds: List<UUID>): List<RequestEntity>

    /**
     * Get all requests by userId.
     * @param userId to filter by
     * @return list of matching RequestEntity objects
     */

    fun findByUserId(userId: UUID): List<RequestEntity>

    /**
     * Return the number of requests that match the optional filters.
     * @param searchFilter to filter by
     * @param companyIds list of companyIds matching the company name search string
     * @param userIds list of userIds matching the user email search string
     * @return number of matching requests
     */
    @Query(
        "SELECT COUNT(request) FROM RequestEntity request " +
            "WHERE " +
            "(:#{#searchFilter.companyId} IS NULL OR request.companyId = :#{#searchFilter.companyId}) AND " +
            "((:#{#searchFilter.dataTypes == null} = TRUE) OR request.dataType IN :#{#searchFilter.dataTypes}) AND " +
            "((:#{#companyIds == null} = TRUE) OR request.companyId IN :#{#companyIds}) AND " +
            "((:#{#searchFilter.reportingPeriods == null} = TRUE) OR request.reportingPeriod IN :#{#searchFilter.reportingPeriods}) AND " +
            "(:#{#searchFilter.userId} IS NULL OR request.userId = :#{#searchFilter.userId}) AND " +
            "((:#{#searchFilter.requestStates == null} = TRUE) OR request.state IN :#{#searchFilter.requestStates}) AND " +
            "((:#{#searchFilter.requestPriorities == null} = TRUE) " +
            "OR request.requestPriority IN :#{#searchFilter.requestPriorities}) AND " +
            "(:#{#searchFilter.adminComment} IS NULL " +
            "OR LOWER(request.adminComment) LIKE LOWER(CONCAT('%', :#{#searchFilter.adminComment}, '%')) ) AND " +
            "((:#{#userIds == null} = TRUE) OR request.userId IN :#{#userIds})",
    )
    fun getNumberOfRequests(
        searchFilter: RequestSearchFilter<UUID>,
        companyIds: List<UUID>? = null,
        userIds: List<UUID>? = null,
    ): Int
}
