package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

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
}
