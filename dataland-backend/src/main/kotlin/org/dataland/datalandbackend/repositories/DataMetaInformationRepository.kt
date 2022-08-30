package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing DataMetaInformationEntities
 */
interface DataMetaInformationRepository : JpaRepository<DataMetaInformationEntity, String> {
    /**
     * Retrieves all meta information for a given company.
     * DOES NOT throw a not-found error if called with a non-existent company ID
     */
    fun getByCompanyCompanyId(companyId: String): List<DataMetaInformationEntity>

    /**
     * Retrieves all meta information for a given company and dataType.
     * DOES NOT throw errors when called with invalid company ids or dataTypes
     */
    fun getByCompanyCompanyIdAndDataType(companyId: String, dataType: String): List<DataMetaInformationEntity>
}
