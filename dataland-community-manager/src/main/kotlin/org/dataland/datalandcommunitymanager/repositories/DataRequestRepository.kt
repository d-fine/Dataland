package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandbackend.repositories.utils.GetDataRequestsSearchFilter
import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
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

    /** This method checks if a data request with the provided params already exists in the database.
     * @param userId to check for
     * @param dataRequestCompanyIdentifierValue to check for
     * @param dataTypeName to check for
     * @returns a Boolean stating the result of the check
     */
    fun existsByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeNameAndReportingPeriod(
        userId: String,
        dataRequestCompanyIdentifierValue: String,
        dataTypeName: String,
        reportingPeriod: String,
    ): Boolean

    /** This method checks if a data request with the provided params already exists in the database.
     * @param userId to check for
     * @param dataRequestCompanyIdentifierValue to check for
     * @param dataTypeName to check for
     * @returns a Boolean stating the result of the check
     */
    fun findByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeNameAndReportingPeriod(
        userId: String,
        dataRequestCompanyIdentifierValue: String,
        dataTypeName: String,
        reportingPeriod: String,
    ): DataRequestEntity?

    /** This method queries data requests and aggregates all the userIds, so that the result contains the count of
     * data requests for one specific identifierValue, identifierType and framework.
     * It also filters these results based on the provided identifier value and frameworks.
     * @param identifierValue to check for
     * @param reportingPeriod to check for
     * @param dataTypeNames to check for
     * @returns the aggregated data requests
     */
    @Query(
        "SELECT new org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity(" +
            "d.dataTypeName, " +
            "d.reportingPeriod, " +
            "d.dataRequestCompanyIdentifierType, " +
            "d.dataRequestCompanyIdentifierValue, " +
            "COUNT(d.userId))" +
            "FROM DataRequestEntity d " +
            "WHERE (:dataTypes IS NULL OR d.dataTypeName IN :dataTypes) " +
            "  AND (:reportingPeriod IS NULL OR d.reportingPeriod LIKE %:reportingPeriod%)" +
            "  AND (:identifierValue IS NULL OR d.dataRequestCompanyIdentifierValue LIKE %:identifierValue%) " +
            "GROUP BY d.dataTypeName, d.reportingPeriod, d.dataRequestCompanyIdentifierType," +
            "  d.dataRequestCompanyIdentifierValue",
    )
    fun getAggregatedDataRequests(
        @Param("identifierValue") identifierValue: String?,
        @Param("dataTypes") dataTypeNames: List<String>?,
        @Param("reportingPeriod") reportingPeriod: String?,
    ): List<AggregatedDataRequestEntity>

    /**
     * A function for searching for data request information by dataType, userID, requestID, requestStatus,
     * reportingPeriod or dataRequestCompanyIdentifierValue
     * @param searchFilter takes the input params to check ofr
     * @returns the data request
     */
    @Query(
        "SELECT d FROM DataRequestEntity d  " +
            "WHERE " +
            "(:#{#searchFilter.dataTypeNameFilterLength} = 0 " +
            "OR d.dataTypeName = :#{#searchFilter.dataTypeNameFilter}) AND " +
            "(:#{#searchFilter.userIdFilterLength} = 0 " +
            "OR d.userId = :#{#searchFilter.userIdFilter}) AND " +
            "(:#{#searchFilter.requestStatus} IS NULL " +
            "OR d.requestStatus = :#{#searchFilter.requestStatus}) AND " +
            "(:#{#searchFilter.reportingPeriodFilterLength} = 0 " +
            "OR d.reportingPeriod = :#{#searchFilter.reportingPeriodFilter}) AND " +
            "(:#{#searchFilter.dataRequestCompanyIdentifierValueFilterLength} =0 " +
            "OR d.dataRequestCompanyIdentifierValue = :#{#searchFilter.dataRequestCompanyIdentifierValueFilter})",
    )
    fun searchDataRequestEntity(
        @Param("searchFilter") searchFilter: GetDataRequestsSearchFilter,
    ): List<DataRequestEntity>

    @Query(
        "SELECT DISTINCT d FROM DataRequestEntity d " +
            "LEFT JOIN FETCH d.messageHistory " +
            "WHERE d IN :dataRequests",
    )
    fun fetchMessages(
        dataRequests: List<DataRequestEntity>,
    ): List<DataRequestEntity>
}
