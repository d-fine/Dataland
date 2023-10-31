package org.dataland.datalandcommunitymanager.repositories

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for storing and retrieving data requests
 */

interface DataRequestRepository : JpaRepository<DataRequestEntity, String> {
    fun findByUserId(userId: String): List<DataRequestEntity>

    fun existsByUserIdAndCompanyIdentifierValueAndDataType(
        userId: String,
        companyIdentifierValue: String,
        dataType: DataTypeEnum,
    ): Boolean
}
