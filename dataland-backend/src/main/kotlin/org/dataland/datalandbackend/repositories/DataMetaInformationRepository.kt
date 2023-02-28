package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
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
     * If companyId is not empty, then only metaInformation for data with that companyId is returned
     * If an invalid companyId is supplied, no results are returned (but no errors is thrown)
     * If dataType is not empty, then only metaInformation for data with that dataType is returned
     * If an invalid dataType is supplied, no results are returned (but no error is thrown)
     * If reportingPeriod is not empty, then only metaInformation for data with for that reportingPeriod is returned
     */
    @Query(
        "SELECT dataMetaInformation FROM DataMetaInformationEntity dataMetaInformation " +
            "WHERE " +
            "(:#{#searchFilter.companyIdFilterLength} = 0 " +
            "OR dataMetaInformation.company.companyId = :#{#searchFilter.companyIdFilter}) AND " +
            "(:#{#searchFilter.dataTypeFilterLength} = 0 " +
            "OR dataMetaInformation.dataType = :#{#searchFilter.dataTypeFilter}) AND " +
            "(:#{#searchFilter.reportingPeriodFilterLength} = 0 " +
            "OR dataMetaInformation.reportingPeriod = :#{#searchFilter.reportingPeriodFilter}) AND " +
            "(:#{#searchFilter.onlyActive} = false " +
            "OR dataMetaInformation.currentlyActive = true)",
    )
    fun searchDataMetaInformation(
        @Param("searchFilter") searchFilter: DataMetaInformationSearchFilter,
    ): List<DataMetaInformationEntity>

    /**
     * Retrieves the currently active dataset for the given triplet of reporting Period, company and dataType
     */
    @Query(
        "SELECT dataMetaInformation FROM DataMetaInformationEntity dataMetaInformation " +
            "WHERE dataMetaInformation.reportingPeriod = :#{#reportingPeriod} " +
            "AND dataMetaInformation.company.companyId = :#{#company.companyId} " +
            "AND dataMetaInformation.dataType = :#{#dataType} " +
            "AND dataMetaInformation.currentlyActive = true ",
    )
    fun getActiveDataset(
        @Param("company") company: StoredCompanyEntity,
        @Param("dataType") dataType: String,
        @Param("reportingPeriod") reportingPeriod: String
    ): DataMetaInformationEntity?
}
