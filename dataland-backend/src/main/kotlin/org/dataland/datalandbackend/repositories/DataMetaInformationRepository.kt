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
     * Searches for dataMetaInformation based on a filter defined in DataMetaInformationSearchFilter.
     * The filtering parameters are companyId, dataType, reportingPeriod, uploaderUserId, qaStatus, active status.
     * - If a filter for companyId, dataType, reportingPeriod, uploaderUserIds, or qaStatus is set (non-null or true),
     *   only dataMetaInformation matching the criteria will be returned.
     * - The onlyActive filter allows filtering for dataMetaInformation that are currently active.
     * - If an invalid or non-matching value is used for any of the filters, no results are returned for that filter,
     *   but no error is thrown.
     * @param searchFilter The set of filtering criteria encapsulated in DataMetaInformationSearchFilter object.
     * @return A list of DataMetaInformationEntity instances that match the specified filter criteria.
     */
    @Query(
        "SELECT dataMetaInformation FROM DataMetaInformationEntity dataMetaInformation " +
            "WHERE " +
            "(:#{#searchFilter.shouldFilterByCompanyId} = false " +
            "OR dataMetaInformation.company.companyId = :#{#searchFilter.preparedCompanyId}) AND " +
            "(:#{#searchFilter.shouldFilterByDataType} = false " +
            "OR dataMetaInformation.dataType = :#{#searchFilter.preparedDataType}) AND " +
            "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
            "OR dataMetaInformation.reportingPeriod = :#{#searchFilter.preparedReportingPeriod}) AND " +
            "(:#{#searchFilter.onlyActive} = false " +
            "OR dataMetaInformation.currentlyActive = true) AND " +
            "(:#{#searchFilter.shouldFilterByUploaderUserIds} = false " +
            "OR dataMetaInformation.uploaderUserId IN :#{#searchFilter.preparedUploaderUserIds}) AND " +
            "(:#{#searchFilter.shouldFilterByQaStatus} = false OR " +
            "dataMetaInformation.qaStatus = :#{#searchFilter.preparedQaStatus})",
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
        value =
            "SELECT " +
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
    fun getUserUploadsDataMetaInfos(userId: String): List<DatasetMetaInfoEntityForMyDatasets>

    @Query(
        "SELECT dataMetaInformation FROM DataMetaInformationEntity dataMetaInformation " +
                "WHERE dataMetaInformation.reportingPeriod = :reportingPeriod " +
                "AND dataMetaInformation.company.companyId = :companyId " +
                "AND dataMetaInformation.dataType = :dataType " +
                "AND dataMetaInformation.currentlyActive = true",
    )
    fun findActiveDatasetByReportingPeriodAndCompanyIdAndDataType(
        @Param("reportingPeriod") reportingPeriod: String,
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: String
    ): DataMetaInformationEntity?
}
