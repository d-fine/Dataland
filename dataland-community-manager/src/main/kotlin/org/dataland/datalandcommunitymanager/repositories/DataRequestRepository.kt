package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.utils.TemporaryTables
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
    @Query(
        // TODO anpassen
        " SELECT new org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity( " +
            "d.dataType, " +
            "d.reportingPeriod, " +
            "d.datalandCompanyId, " +
            "rs.requestStatus, " +
            "COUNT(d.userId) " +
            ") " +
            "FROM DataRequestEntity d " +
            "JOIN RequestStatusEntity rs ON d = rs.dataRequest " +
            "WHERE (:dataTypes IS NULL OR d.dataType IN :dataTypes) " +
            "AND (:reportingPeriod IS NULL OR d.reportingPeriod LIKE %:reportingPeriod%) " +
            "AND (:identifierValue IS NULL OR d.datalandCompanyId LIKE %:identifierValue%) " +
            "AND rs.creationTimestamp =  ( " +
            "SELECT MAX(rs2.creationTimestamp) " +
            "FROM RequestStatusEntity rs2 " +
            "WHERE rs.dataRequest = rs2.dataRequest " +
            ") " +
            "AND (:status IS NULL OR rs.requestStatus = :status) " +
            "GROUP BY d.dataType, d.reportingPeriod, d.datalandCompanyId, rs.requestStatus ",
    )
    fun getAggregatedDataRequests(
        @Param("identifierValue") identifierValue: String?,
        @Param("dataTypes") dataTypes: Set<String>?,
        @Param("reportingPeriod") reportingPeriod: String?,
        @Param("status") status: RequestStatus?,
    ): List<AggregatedDataRequestEntity>

    /**
     * A function for searching for data request information by dataType, userID, requestID, requestStatus,
     * accessStatus, reportingPeriod or dataRequestCompanyIdentifierValue
     * @param searchFilter takes the input params to check for
     * @returns the data request
     */
    @Query(
        nativeQuery = true,
        value = TemporaryTables.TABLE_FILTERED +
            // TODO alles hier drüber als string konstante auslagern und abrufbar machen, das auskommentierte select
            // TODO hier drunter verwenden für die eigentliche funktionalität der funktion
            // TODO die ausgelagerte komponten in allen funktionen wie zB fetchStatusHistory verwenden
            // TODO an den code stellen wie zB im DataRequestQueryManager nur noch einmal eine Query aufrufen,
            // TODO  nicht zweimal Line 140
            // TODO andere verwendungen von searchDataRequestEntity finden und genauso anpassen

            "SELECT d.* FROM data_requests d " +
            "JOIN filtered_table ON filtered_table.data_request_id = d.data_request_id",

    )
    fun searchDataRequestEntity(
        @Param("searchFilter") searchFilter: GetDataRequestsSearchFilter,
    ): List<DataRequestEntity>

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
    fun fetchStatusHistory(
        dataRequests: List<DataRequestEntity>,
    ): List<DataRequestEntity>

    /**
     * Fetches data request entities together with the associated status history
     * @param searchFilter the search filter used to filter in the data_requests table
     * @returns a list of data request entities together with their associated status history
     */
    @Query(
        nativeQuery = true,
        value = TemporaryTables.TABLE_FILTERED +

            "SELECT DISTINCT d.* " +
            "FROM data_requests d " +
            "LEFT JOIN request_status_history rsh ON d.data_request_id = rsh.data_request_id " +
            "INNER JOIN filtered_table ft ON d.data_request_id = ft.data_request_id ",

    )
    fun searchDataRequestEntityAndStatusHistory(
        @Param("searchFilter") searchFilter: GetDataRequestsSearchFilter,
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
