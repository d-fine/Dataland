package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DatasetMetaInfoEntityForMyDatasets
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
     * If an invalid companyId is supplied, no results are returned (but no error is thrown)
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
        @Param("reportingPeriod") reportingPeriod: String,
    ): DataMetaInformationEntity?

    /**
     * Counts the dataset meta information
     * filtered by company ID, data type and if it is currently active
     * @param companyId the ID of the company to filter for
     * @param dataType the data type to filter for
     * @param currentlyActive the currently active filter
     * @returns the number of data meta informations that fulfill these criteria
     */
    @Query(
        "SELECT COUNT(d) FROM DataMetaInformationEntity d " +
            "WHERE d.company.companyId = ?1 AND d.dataType = ?2 AND d.currentlyActive = ?3",
    )
    fun countByCompanyIdAndDataTypeAndCurrentlyActive(
        companyId: String,
        dataType: String,
        currentlyActive: Boolean,
    ): Long

    /**
     * Queries the meta information for datasets uploaded by a specific user
     * @param userId the id of the user for whom to query data meta information
     * @returns the data meta information uploaded by the specified user
     */
    @Query(
        nativeQuery = true,
        value = "SELECT " +
            " datainfo.data_id as dataId," +
            " datainfo.company_id as companyId," +
            " company.company_name as companyName," +
            " datainfo.data_type as dataType," +
            " datainfo.reporting_period as reportingPeriod," +
            " datainfo.quality_status as qualityStatus," +
            " datainfo.currently_active as currentlyActive," +
            " datainfo.upload_time as uploadTime" +
            " from (" +
            " SELECT company_id, data_id, data_type, reporting_period, quality_status, currently_active, upload_time " +
            " from data_meta_information meta" +
            " where uploader_user_id = :userId" +
            " ) datainfo" +
            " LEFT JOIN stored_companies company" +
            " ON company.company_id = datainfo.company_id",
    )
    fun getUserUploadsDataMetaInfos(
        userId: String,
    ): List<DatasetMetaInfoEntityForMyDatasets>

    /**
     * Returns all available distinct country codes
     */
    @Query(
        "SELECT DISTINCT company.countryCode FROM StoredCompanyEntity company " +
            "INNER JOIN company.dataRegisteredByDataland data ",
    )
    fun fetchDistinctCountryCodes(): Set<String>

    /**
     * Returns all available distinct sectors
     */
    @Query(
        "SELECT DISTINCT company.sector FROM StoredCompanyEntity company " +
            "INNER JOIN company.dataRegisteredByDataland data " +
            "WHERE company.sector IS NOT NULL ",
    )
    fun fetchDistinctSectors(): Set<String>
}
