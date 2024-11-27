package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * A JPA repository for accessing QA information of a data point
 */
interface DataPointQaReviewRepository : JpaRepository<DataPointQaReviewEntity, UUID> {
    /**
     * A function to get the dataId of the currently active data point, given a triple of company ID, data type and reporting period
     * @param searchFilter the filter to apply to the search containing the company ID, data type, reporting period and the QA status
     */
    @Query(
        "SELECT dataPointQaReview.dataId FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE dataPointQaReview.companyId = ':#{#searchFilter.companyId}' " +
            "AND dataPointQaReview.dataPointIdentifier = ':#{#searchFilter.dataPointIdentifier}' " +
            "AND dataPointQaReview.reportingPeriod = ':#{#searchFilter.reportingPeriod}' " +
            "AND dataPointQaReview.qaStatus = 'Accepted' " +
            "ORDER BY dataPointQaReview.timestamp DESC ",
    )
    fun getDataIdOfCurrentlyActiveDataPoint(
        @Param("searchFilter") searchFilter: DataPointFilter,
    ): String?
}
