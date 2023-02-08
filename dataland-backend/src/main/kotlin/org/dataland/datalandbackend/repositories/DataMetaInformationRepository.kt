package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing DataMetaInformationEntities
 */
interface DataMetaInformationRepository : JpaRepository<DataMetaInformationEntity, String> {

    /**
     * A function for searching for dataMetaInformation by dataType and companyId.
     * If dataType is not empty, then only metaInformation for data with that dataType is returned
     * If an invalid dataType is supplied, no results are returned (but no error is thrown)
     * If companyId is not empty, then only metaInformation for data with that companyId is returned
     * If an invalid companyId is supplied, no results are returned (but no errors is thrown)
     */
    @Query(
        "SELECT dataMetaInformation FROM DataMetaInformationEntity dataMetaInformation " +
            "WHERE " +
            "(:#{#searchFilter.dataTypeFilterLength} = 0 " +
            "OR dataMetaInformation.dataType = :#{#searchFilter.dataTypeFilter}) AND " +
            "(:#{#searchFilter.companyIdFilterLength} = 0 " +
            "OR dataMetaInformation.company.companyId = :#{#searchFilter.companyIdFilter})",
    )
    fun searchDataMetaInformation(
        @Param("searchFilter") searchFilter: DataMetaInformationSearchFilter,
    ): List<DataMetaInformationEntity>
}
