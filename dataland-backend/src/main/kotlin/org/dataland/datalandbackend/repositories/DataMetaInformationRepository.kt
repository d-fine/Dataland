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
     * Retrieves all data meta information that were not migrated to data points yet
     */
    @Query(
        "SELECT dataMetaInformation FROM DataMetaInformationEntity dataMetaInformation " +
            "FULL JOIN DatasetDatapointEntity entity ON dataMetaInformation.dataId = entity.datasetId " +
            "WHERE entity IS NULL AND dataMetaInformation.dataType IN :allowedDataTypes",
    )
    fun getAllDataMetaInformationThatDoNotHaveDataPoints(allowedDataTypes: List<String>): List<DataMetaInformationEntity>

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

    /**
     * Retrieves active datasets matching the JSON-encoded list of dataset dimensions.
     */
    @Query(
        nativeQuery = true,
        value =
            """
            WITH requested AS (
                SELECT DISTINCT company_id, framework, reporting_period
                FROM jsonb_to_recordset(CAST(:jsonPayload AS jsonb))
                    AS dim(company_id text, framework text, reporting_period text)
            )
            SELECT m.*
            FROM requested dim
            JOIN data_meta_information m
                ON m.company_id = dim.company_id
                AND m.data_type = dim.framework
                AND m.reporting_period = dim.reporting_period
            WHERE m.currently_active = true
            """,
    )
    fun findActiveDatasetsByDimensionsJson(
        @Param("jsonPayload") jsonPayload: String,
    ): List<DataMetaInformationEntity>

    /**
     * Retrieves active dataset dimensions matching the given filter criteria.
     * An empty JSON array for any filter means "match all" (wildcard).
     */
    @Query(
        nativeQuery = true,
        value =
            """
            WITH
                company_filter AS (
                    SELECT value AS company_id
                    FROM jsonb_array_elements_text(CAST(:companyIds AS jsonb))
                ),
                type_filter AS (
                    SELECT value AS data_type
                    FROM jsonb_array_elements_text(CAST(:dataTypes AS jsonb))
                ),
                period_filter AS (
                    SELECT value AS reporting_period
                    FROM jsonb_array_elements_text(CAST(:reportingPeriods AS jsonb))
                )
            SELECT m.*
            FROM data_meta_information m
            WHERE m.currently_active = true
              AND (jsonb_array_length(CAST(:companyIds AS jsonb)) = 0 
                OR m.company_id IN (SELECT company_id FROM company_filter))
              AND (jsonb_array_length(CAST(:dataTypes AS jsonb)) = 0 
                OR m.data_type IN (SELECT data_type FROM type_filter))
              AND (jsonb_array_length(CAST(:reportingPeriods AS jsonb)) = 0 
                OR m.reporting_period IN (SELECT reporting_period FROM period_filter))
            """,
    )
    fun findActiveDatasetDimensionsByFilter(
        @Param("companyIds") companyIds: String,
        @Param("dataTypes") dataTypes: String,
        @Param("reportingPeriods") reportingPeriods: String,
    ): List<DataMetaInformationEntity>

    /**
     * Retrieves the most recent (in terms of reporting period) active DataMetaInformationEntity for each company and the specified dataType
     *
     * @param companyIds the list of company IDs
     * @param dataType the data type
     * @returns a list of DataMetaInformationEntity representing the latest active dataset for each company
     */
    @Query(
        """
    SELECT d FROM DataMetaInformationEntity d
    WHERE d.company.companyId IN :companyIds
      AND d.dataType = :dataType
      AND d.currentlyActive = true
      AND d.reportingPeriod = (
        SELECT MAX(d2.reportingPeriod) FROM DataMetaInformationEntity d2
        WHERE d2.company.companyId = d.company.companyId
          AND d2.dataType = :dataType
          AND d2.currentlyActive = true
      )
    """,
    )
    fun findLatestActiveByCompanyIdsAndDataType(
        @Param("companyIds") companyIds: Collection<String>,
        @Param("dataType") dataType: String,
    ): List<DataMetaInformationEntity>
}
