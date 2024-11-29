package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
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
     * @param filter the filter to apply to the search containing the company ID, data type, reporting period and the QA status
     */
    @Query(
        "SELECT dataPointQaReview.dataId FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE dataPointQaReview.companyId = ':#{#filter.companyId}' " +
            "AND dataPointQaReview.dataPointIdentifier = ':#{#filter.dataPointIdentifier}' " +
            "AND dataPointQaReview.reportingPeriod = ':#{#filter.reportingPeriod}' " +
            "AND dataPointQaReview.qaStatus = 'Accepted' " +
            "ORDER BY dataPointQaReview.timestamp DESC " +
            "LIMIT 1",
    )
    fun getDataIdOfCurrentlyActiveDataPoint(
        @Param("filter") filter: DataPointQaReviewItemFilter,
    ): String?

    /**
     * Find QA information for a specific dataId. Take all entries ordered by descending timestamp.
     * @param dataId ID to specify the data point the QA information is for
     */
    fun findByDataIdOrderByTimestampDesc(dataId: String): List<DataPointQaReviewEntity>

    /**
     * Find all QA information items for all data points currently in the QA status provided by [qaStatus].
     * Is specifically used to find all data points that are currently in the 'Pending' QA status (the review queue).
     * @param qaStatus the QA status to filter for
     */
    fun findByQaStatusOrderByTimestampDesc(qaStatus: QaStatus): List<DataPointQaReviewEntity>

    /**
     * Find all QA information items filtering by company ID, data point identifier, reporting period and QA status provided via [filter].
     * Results are paginated using [resultLimit] and [resultOffset].
     * @param filter the filter to apply to the search containing the company ID, data point identifier, reporting period and the QA status
     * @param resultLimit the maximum number of results to return
     * @param resultOffset the offset to start the result set from
     */
    @Query(
        "SELECT dataPointQaReview FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE (:#{#filter.companyId} IS NULL OR dataPointQaReview.companyId = ':#{#filter.companyId}') " +
            "AND (:#{#filter.dataPointIdentifier} IS NULL OR dataPointQaReview.dataPointIdentifier = ':#{#filter.dataPointIdentifier}') " +
            "AND (:#{#filter.reportingPeriod} IS NULL OR dataPointQaReview.reportingPeriod = ':#{#filter.reportingPeriod}') " +
            "AND (:#{#filter.qaStatus} IS NULL OR dataPointQaReview.qaStatus = ':#{#filter.qaStatus}')" +
            "ORDER BY dataPointQaReview.timestamp DESC " +
            "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun findByFilters(
        @Param("filter") filter: DataPointQaReviewItemFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<DataPointQaReviewEntity>
}
