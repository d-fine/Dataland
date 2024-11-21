package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.repositories.utils.TemporaryTables
import org.dataland.datalandcommunitymanager.repositories.utils.TemporaryTables.Companion.MOST_RECENT_STATUS_CHANGE
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandcommunitymanager.utils.GetAggregatedRequestsSearchFilter
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

    @Query(
        nativeQuery = true,
        value =
            MOST_RECENT_STATUS_CHANGE +
                "SELECT " +
                "dr.data_type AS dataType, " +
                "dr.reporting_period AS reportingPeriod, " +
                "dr.dataland_company_id AS datalandCompanyId, " +
                "st.request_status AS requestStatus, " +
                "COUNT(dr.user_id) AS count " +
                "FROM data_requests dr " +
                "JOIN status_table st ON dr.data_request_id = st.request_id " +
                "WHERE (:#{#searchFilter.dataTypeFilterLength} = 0 " +
                "OR dr.data_type IN :#{#searchFilter.dataTypeFilter} ) " +
                "AND (:#{#searchFilter.reportingPeriodFilterLength} = 0 " +
                "OR dr.reporting_period = :#{#searchFilter.reportingPeriodFilter}) " +
                "AND (:#{#searchFilter.datalandCompanyIdFilterLength} = 0 " +
                "OR dr.dataland_company_id = :#{#searchFilter.datalandCompanyIdFilter}) " +
                "AND (:#{#searchFilter.requestStatusLength} = 0 " +
                "OR st.request_status = :#{#searchFilter.requestStatus} ) " +
                "GROUP BY dr.data_type, dr.reporting_period, dr.dataland_company_id, st.request_status ",
    )
    fun getAggregatedDataRequests(
        @Param("searchFilter") searchFilter: GetAggregatedRequestsSearchFilter,
    ): List<AggregatedDataRequest>

    /**
     * A function for searching for data request information by dataType, userID, requestID, requestStatus,
     * accessStatus, reportingPeriod or dataRequestCompanyIdentifierValue
     * @param searchFilter takes the input params to check for
     * @param resultLimit The number of entities that should be returned
     * @param resultOffset The offset of the returned entities
     * @returns the data request
     */
    @Query(
        nativeQuery = true,
        value =
            TemporaryTables.TABLE_FILTERED +
                TemporaryTables.TABLE_FILTERED_ORDER_AND_LIMIT + TemporaryTables.TABLE_FILTERED_END +
                "SELECT d.* FROM data_requests d " +
                "JOIN filtered_table ON filtered_table.data_request_id = d.data_request_id ",
    )
    fun searchDataRequestEntity(
        @Param("searchFilter") searchFilter: DataRequestsFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<DataRequestEntity>

    /**
     * This query counts the number of requests that matches the search fiter and returns this number.
     */
    @Query(
        nativeQuery = true,
        value =
            TemporaryTables.TABLE_FILTERED + TemporaryTables.TABLE_FILTERED_END +
                "SELECT COUNT(*) FROM data_requests d " +
                "JOIN filtered_table ON filtered_table.data_request_id = d.data_request_id ",
    )
    fun getNumberOfRequests(
        @Param("searchFilter") searchFilter: DataRequestsFilter,
    ): Int

    /**
     * Fetches data request entities together with the associated status history
     * @param dataRequests the requests entities for which the status histories to fetch
     * @returns the initial list of data request entities together with the associated status history
     */
    @Query(
        "SELECT DISTINCT d FROM DataRequestEntity d " +
            "LEFT JOIN FETCH d.dataRequestStatusHistory " +
            "WHERE d IN :dataRequests",
    )
    fun fetchStatusHistory(dataRequests: List<DataRequestEntity>): List<DataRequestEntity>

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
