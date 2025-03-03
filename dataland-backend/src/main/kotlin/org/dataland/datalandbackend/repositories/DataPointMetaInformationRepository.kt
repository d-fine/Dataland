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
     * Retrieves all data meta information of active data points matching one of the data point types and the company
     */
    fun findByDataPointTypeInAndCompanyIdAndCurrentlyActiveTrue(
        dataPointTypes: Set<String>,
        companyId: String,
    ): List<DataPointMetaInformationEntity>
}
