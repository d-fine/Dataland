package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
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
     * @param dataType to check for
     * @returns a Boolean stating the result of the check
     */
    fun existsByUserIdAndDataRequestCompanyIdentifierValueAndDataType(
        userId: String,
        dataRequestCompanyIdentifierValue: String,
        dataType: DataTypeEnum,
    ): Boolean

    /** This method queries data requests and aggregates all the userIds, so that the result contains the count of
     * data requests for one specific identifierValue, identifierType and framework.
     * It also filters these results based on the provided identifier value and frameworks.
     * @param identifierValue to check for
     * @param dataTypes to check for
     * @returns the aggregated data requests
     */
    @Query(
        "SELECT new org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest(" +
            "d.dataType, " +
            "d.dataRequestCompanyIdentifierType, " +
            "d.dataRequestCompanyIdentifierValue, " +
            "COUNT(d.userId))" +
            "FROM DataRequestEntity d " +
            "WHERE (:dataTypes IS NULL OR d.dataType IN :dataTypes) " +
            "  AND (:identifierValue IS NULL OR d.dataRequestCompanyIdentifierValue LIKE %:identifierValue%) " +
            "GROUP BY d.dataType, d.dataRequestCompanyIdentifierType, d.dataRequestCompanyIdentifierValue",
    )
    fun getAggregatedDataRequests(
        @Param("identifierValue") identifierValue: String?,
        @Param("dataTypes") dataTypes: Set<DataTypeEnum>?,
    ): List<AggregatedDataRequest>
}
