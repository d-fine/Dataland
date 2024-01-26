package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequestEntity
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
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
    /** This method gets all data request that are stored for a specific framework.
     * @param dataTypeName defines for which framework the data request shall be retrieved
     * @returns a list with all data request for that framework
     */
    fun findByDataTypeName(dataTypeName: String): List<DataRequestEntity>
    /** This method gets all data request that are stored for a specific Reporting Period.
     * @param reportingPeriod defines for which Reporting Period the data request shall be retrieved
     * @returns a list with all data request for that Reporting Period
     */
    fun findByReportingPeriod(reportingPeriod: String): List<DataRequestEntity>
    /** This method gets all data request that are stored for a specific requestStatus.
     * @param requestStatus defines for which requestStatus the data request shall be retrieved
     * @returns a list with all data request for that requestStatus
     */
    fun findByRequestStatus(requestStatus: RequestStatus): List<DataRequestEntity>
    /** This method gets all data request that are stored for a specific CompanyIdentifierValue.
     * @param dataRequestCompanyIdentifierValue defines for which CompanyIdentifierValue the data request shall be retrieved
     * @returns a list with all data request for that CompanyIdentifierValue
     */
    fun findByDataRequestCompanyIdentifierValue(dataRequestCompanyIdentifierValue: String): List<DataRequestEntity>
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

    /** This method queries data requests and aggregates all the userIds, so that the result contains the count of
     * data requests for one specific identifierValue, identifierType and framework.
     * It also filters these results based on the provided identifier value and frameworks.
     * @param identifierValue to check for
     * @param reportingPeriod to check for
     * @param dataTypes to check for
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
}
