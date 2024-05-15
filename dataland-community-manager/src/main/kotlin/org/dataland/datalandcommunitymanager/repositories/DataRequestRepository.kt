package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for storing and retrieving data requests
 */

interface DataRequestRepository : JpaRepository<DataRequestEntity, String> {
    /** This method gets all data request that are stored for a specific userId.
     * @param userId defines for which user the data request shall be retrieved
     * @returns a list with all data request for that user
     */
    fun findByUserId(userId: String): List<DataRequestEntity>

    /** This method looks for data requests with the provided params already exist in the database.
     * @param userId to check for
     * @param datalandCompanyId to check for
     * @param dataType to check for
     * @param reportingPeriod to check for
     * @returns a list of the data requests
     */
    fun findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
        userId: String,
        datalandCompanyId: String,
        dataType: String,
        reportingPeriod: String,
    ): List<DataRequestEntity>?

    /** This method queries data requests and aggregates all the userIds, so that the result contains the count of
     * data requests for one specific identifierValue, identifierType and framework.
     * It also filters these results based on the provided identifier value and frameworks.
     * @param reportingPeriod to check for
     * @param dataTypes to check for
     * @param status to check for
     * @returns the aggregated data requests
     */
    // todo
    @Query(
        """
    SELECT new org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity(
        d.dataType, 
        d.reportingPeriod, 
        d.datalandCompanyId,
         h.requestStatus,  
        COUNT(d.userId)
    ) 
    FROM DataRequestEntity d
     left join RequestStatusEntity h on d.dataRequestId = h.dataRequestStatus.dataRequestId
    WHERE (:dataTypes IS NULL OR d.dataType IN :dataTypes) 
      AND (:reportingPeriod IS NULL OR d.reportingPeriod LIKE %:reportingPeriod%) 
      AND (:identifierValue IS NULL OR d.datalandCompanyId LIKE %:identifierValue%)
      AND h.creationTimestamp =  (
    SELECT MAX(h2.creationTimestamp)
    FROM RequestStatusEntity h2
    WHERE h.dataRequestStatus.dataRequestId = h2.dataRequestStatus.dataRequestId
)
AND (:status IS NULL OR h.requestStatus = :status)
    GROUP BY d.dataType, d.reportingPeriod, d.datalandCompanyId, h.requestStatus
""",
    )
    fun getAggregatedDataRequests(
        @Param("identifierValue") identifierValue: String?,
        @Param("dataTypes") dataTypes: Set<String>?,
        @Param("reportingPeriod") reportingPeriod: String?,
        @Param("status") status: RequestStatus?,
    ): List<AggregatedDataRequestEntity>

    /**
     * A function for searching for data request information by dataType, userID, requestID, requestStatus,
     * reportingPeriod or dataRequestCompanyIdentifierValue
     * @param searchFilter takes the input params to check ofr
     * @returns the data request
     */
    @Query(
        "SELECT d FROM DataRequestEntity d  " +
            "JOIN RequestStatusEntity rs ON d.dataRequestId = rs.dataRequestStatus " +
            "WHERE " +
            "(:#{#searchFilter.dataTypeFilterLength} = 0 " +
            "OR d.dataType = :#{#searchFilter.dataTypeFilter}) AND " +
            "(:#{#searchFilter.userIdFilterLength} = 0 " +
            "OR d.userId = :#{#searchFilter.userIdFilter}) AND " +
            "(:#{#searchFilter.requestStatus} IS NULL " +
            "OR rs.dataRequestStatus = :#{#searchFilter.requestStatus}) AND " +
            "(:#{#searchFilter.reportingPeriodFilterLength} = 0 " +
            "OR d.reportingPeriod = :#{#searchFilter.reportingPeriodFilter}) AND " +
            "(:#{#searchFilter.datalandCompanyIdFilterLength} = 0 " +
            "OR d.datalandCompanyId = :#{#searchFilter.datalandCompanyIdFilter})",
    )
    fun searchDataRequestEntity(
        @Param("searchFilter") searchFilter: GetDataRequestsSearchFilter,
    ): List<DataRequestEntity>

    /**
     * Fetches data request entities together with the associated message history
     * @param dataRequests the requests entities for which the message histories to fetch
     * @returns the initial list of data request entities together with the associated message history
     */
    @Query(
        "SELECT DISTINCT d FROM DataRequestEntity d " +
            "LEFT JOIN FETCH d.messageHistory " +
            "WHERE d IN :dataRequests",
    )
    fun fetchMessages(
        dataRequests: List<DataRequestEntity>,
    ): List<DataRequestEntity>

    /** This method counts the number of data requests that a user
     * has performed from a specified timestamp.
     * @param userId to check for
     * @param timestamp to check for
     * @returns the number of counts
     */
    @Query
    (
        "SELECT COUNT(d.userId) FROM DataRequestEntity d " +
            "WHERE (d.userId = :#{#userId})" +
            "AND (d.creationTimestamp >= :#{#timestamp})",
    )
    fun getNumberOfDataRequestsPerformedByUserFromTimestamp(
        userId: String,
        timestamp: Long,
    ): Int
}
