package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing DataMetaInformationEntities
 */
interface DataPointMetaInformationRepository : JpaRepository<DataPointMetaInformationEntity, String> {
    /**
     * Retrieve the ID of the currently active data point for the given data point dimension
     */
    @Query(
        "SELECT dataPointMetaInformation.dataPointId FROM DataPointMetaInformationEntity dataPointMetaInformation " +
            "WHERE dataPointMetaInformation.reportingPeriod = :#{#searchFilter.reportingPeriod} " +
            "AND dataPointMetaInformation.companyId = :#{#searchFilter.companyId} " +
            "AND dataPointMetaInformation.dataPointType = :#{#searchFilter.dataPointType} " +
            "AND dataPointMetaInformation.currentlyActive = true ",
    )
    fun getActiveDataPointId(
        @Param("searchFilter") searchFilter: BasicDataPointDimensions,
    ): String?

    /**
     * Retrieves active data points matching the JSON-encoded list of data point dimensions.
     */
    @Query(
        nativeQuery = true,
        value =
            """
            WITH requested AS (
                SELECT DISTINCT company_id, data_point_type, reporting_period
                FROM jsonb_to_recordset(CAST(:jsonPayload AS jsonb))
                    AS dim(company_id text, data_point_type text, reporting_period text)
            )
            SELECT m.*
            FROM requested dim
            JOIN data_point_meta_information m
                ON m.company_id = dim.company_id
                AND m.data_point_type = dim.data_point_type
                AND m.reporting_period = dim.reporting_period
            WHERE m.currently_active = true
            """,
    )
    fun findActiveDataPointsByDimensionsJson(
        @Param("jsonPayload") jsonPayload: String,
    ): List<DataPointMetaInformationEntity>

    /**
     * Retrieves active data point dimensions matching the given filter criteria.
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
                    SELECT value AS data_point_type
                    FROM jsonb_array_elements_text(CAST(:dataPointTypes AS jsonb))
                ),
                period_filter AS (
                    SELECT value AS reporting_period
                    FROM jsonb_array_elements_text(CAST(:reportingPeriods AS jsonb))
                )
            SELECT m.*
            FROM data_point_meta_information m
            WHERE m.currently_active = true
              AND (jsonb_array_length(CAST(:companyIds AS jsonb)) = 0 OR m.company_id IN (SELECT company_id FROM company_filter))
              AND (jsonb_array_length(CAST(:dataPointTypes AS jsonb)) = 0 OR m.data_point_type IN (SELECT data_point_type FROM type_filter))
              AND (jsonb_array_length(CAST(:reportingPeriods AS jsonb)) = 0 OR m.reporting_period IN (SELECT reporting_period FROM period_filter))
            """,
    )
    fun findActiveDataPointDimensionsByFilter(
        @Param("companyIds") companyIds: String,
        @Param("dataPointTypes") dataPointTypes: String,
        @Param("reportingPeriods") reportingPeriods: String,
    ): List<DataPointMetaInformationEntity>

    /**
     * Retrieves all data meta information of active data points matching one of the data point types and the company
     */
    fun findByDataPointTypeInAndCompanyIdAndCurrentlyActiveTrue(
        dataPointTypes: Set<String>,
        companyId: String,
    ): List<DataPointMetaInformationEntity>

    /**
     * Retrieves all data meta information of active data points matching one of the data point types, reporting period and the company
     */
    fun findByDataPointTypeInAndCompanyIdAndReportingPeriodAndCurrentlyActiveTrue(
        dataPointTypes: Set<String>,
        companyId: String,
        reportingPeriod: String,
    ): List<DataPointMetaInformationEntity>

    /**
     * Retrieves all data meta information of active data points matching one of the provided data point types and the company IDs
     */
    fun findByCompanyIdInAndDataPointTypeInAndCurrentlyActiveTrue(
        companyId: Collection<String>,
        dataPointTypes: Set<String>,
    ): List<DataPointMetaInformationEntity>
}
