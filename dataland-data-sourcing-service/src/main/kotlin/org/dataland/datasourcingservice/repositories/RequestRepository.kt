package org.dataland.datasourcingservice.repositories

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
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
     * @return list of matching request ids
     */
    @Query(
        "SELECT request.id FROM RequestEntity request " +
            "WHERE " +
            "(:#{#searchFilter.companyId} IS NULL OR request.companyId = :#{#searchFilter.companyId}) AND " +
            "((:#{#searchFilter.dataTypes == null} = TRUE) OR request.dataType IN :#{#searchFilter.dataTypes}) AND " +
            "(" +
            "(:#{#searchFilter.reportingPeriods == null} = TRUE) OR " +
            "request.reportingPeriod IN :#{#searchFilter.reportingPeriods}" +
            ") AND " +
            "(:#{#searchFilter.userId} IS NULL OR request.userId = :#{#searchFilter.userId}) AND " +
            "((:#{#searchFilter.requestStates == null} = TRUE) OR request.state IN :#{#searchFilter.requestStates}) AND " +
            "((:#{#searchFilter.requestPriorities == null} = TRUE) OR request.requestPriority IN :#{#searchFilter.requestPriorities})",
    )
    fun searchRequests(
        searchFilter: RequestSearchFilter<UUID>,
        pageable: Pageable,
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
     * Return the number of requests that match the optional filters.
     * @param companyId to filter by
     * @param dataType to filter by
     * @param reportingPeriod to filter by
     * @param state to filter by
     * @return number of matching requests
     */
    @Query(
        "SELECT COUNT(request) FROM RequestEntity request " +
            "WHERE " +
            "(:companyId IS NULL OR request.companyId = :companyId) AND " +
            "(:dataType IS NULL OR request.dataType = :dataType) AND " +
            "(:reportingPeriod IS NULL OR request.reportingPeriod = :reportingPeriod) AND " +
            "(:state IS NULL OR request.state = :state) AND " +
            "(:priority IS NULL OR request.requestPriority = :priority)",
    )
    fun getNumberOfRequests(
        companyId: UUID?,
        dataType: String?,
        reportingPeriod: String?,
        state: RequestState?,
        priority: RequestPriority?,
    ): Int
}
