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
     * Retrieve all entities of active data points associated with the companyIds, dataPointTypes and reportingPeriods
     */
    @Query(
        "SELECT dataPointMetaInformation FROM DataPointMetaInformationEntity dataPointMetaInformation " +
            "WHERE (:#{#reportingPeriods == null || #reportingPeriods.isEmpty()} = true " +
            "OR dataPointMetaInformation.reportingPeriod IN :#{#reportingPeriods}) " +
            "AND (:#{#companyIds == null || #companyIds.isEmpty()} = true " +
            "OR dataPointMetaInformation.companyId IN :#{#companyIds}) " +
            "AND (:#{#dataPointTypes == null || #dataPointTypes.isEmpty()} = true " +
            "OR dataPointMetaInformation.dataPointType IN :#{#dataPointTypes}) " +
            "AND dataPointMetaInformation.currentlyActive = true",
    )
    fun getBulkActiveDataPoints(
        @Param("companyIds") companyIds: List<String>?,
        @Param("dataPointTypes") dataPointTypes: List<String>?,
        @Param("reportingPeriods") reportingPeriods: List<String>?,
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
}
