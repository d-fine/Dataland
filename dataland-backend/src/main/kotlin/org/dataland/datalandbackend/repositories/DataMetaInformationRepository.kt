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
            "OR dataMetaInformation.reportingPeriod = :#{#searchFilter.reportingPeriodFilter})"
    )
    fun searchDataMetaInformation(
        @Param("searchFilter") searchFilter: DataMetaInformationSearchFilter,
    ): List<DataMetaInformationEntity>

    /**
     * A function for searching for dataMetaInformation by dataType and companyId.
     * This function only searches for dataMetaInformation Entities that are active (i.e. ones that are the latest
     * for a pair of companyId, dataType and reporting Period)
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
            "OR dataMetaInformation.reportingPeriod = :#{#searchFilter.reportingPeriodFilter}) " +
            "AND dataMetaInformation.dataId NOT IN (" +
            "SELECT dm1.dataId FROM DataMetaInformationEntity dm1, DataMetaInformationEntity dm2 " +
            "WHERE " +
            "dm1.company.companyId = dm2.company.companyId AND dm1.dataType = dm2.dataType AND dm1.reportingPeriod = dm2.reportingPeriod AND " +
            "dm1.uploadTime < dm2.uploadTime" +
            ")",
    )
    fun searchActiveDataMetaInformation(
        @Param("searchFilter") searchFilter: DataMetaInformationSearchFilter,
    ): List<DataMetaInformationEntity>
}
