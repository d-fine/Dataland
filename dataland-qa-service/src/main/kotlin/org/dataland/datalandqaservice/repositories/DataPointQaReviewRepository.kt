package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A JPA repository for accessing QA information of a data point
 */
@Repository
interface DataPointQaReviewRepository : JpaRepository<DataPointQaReviewEntity, UUID> {
    /**
     * A function to get the dataPointId of the currently active data point, given a triple of company ID, data type and reporting period
     * @param filter the filter to apply to the search containing the company ID, data type, reporting period and the QA status
     */
    @Query(
        "SELECT dataPointQaReview.dataPointId FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE dataPointQaReview.timestamp = " +
            "(SELECT MAX(subDataPointQaReview.timestamp) FROM DataPointQaReviewEntity subDataPointQaReview " +
            "WHERE subDataPointQaReview.dataPointId = dataPointQaReview.dataPointId) " +
            "AND dataPointQaReview.companyId = :#{#filter.companyId} " +
            "AND dataPointQaReview.dataPointType = :#{#filter.dataPointType} " +
            "AND dataPointQaReview.reportingPeriod = :#{#filter.reportingPeriod} " +
            "AND dataPointQaReview.qaStatus = org.dataland.datalandbackendutils.model.QaStatus.Accepted " +
            "ORDER BY dataPointQaReview.timestamp DESC " +
            "LIMIT 1",
    )
    fun getDataPointIdOfCurrentlyActiveDataPoint(
        @Param("filter") filter: BasicDataPointDimensions,
    ): String?

    /**
     * Find QA information for a specific dataPointId. Take all entries ordered by descending timestamp.
     * @param dataPointId ID to specify the data point the QA information is for
     */
    fun findByDataPointIdOrderByTimestampDesc(dataPointId: String): List<DataPointQaReviewEntity>

    /**
     * Find QA information for a specific dataPointId. Take first entry ordered by descending timestamp.
     * @param dataId ID to specify the data point the QA information is for
     */
    fun findFirstByDataPointIdOrderByTimestampDesc(dataId: String): DataPointQaReviewEntity?

    /**
     * Find the latest QA information items per dataPointId and filter for the QA status 'Pending'. These entries form the review queue.
     */
    @Query(
        "SELECT dataPointQaReview FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE dataPointQaReview.timestamp = " +
            "(SELECT MAX(subDataPointQaReview.timestamp) FROM DataPointQaReviewEntity subDataPointQaReview " +
            "WHERE subDataPointQaReview.dataPointId = dataPointQaReview.dataPointId) " +
            "AND dataPointQaReview.qaStatus = org.dataland.datalandbackendutils.model.QaStatus.Pending " +
            "ORDER BY dataPointQaReview.timestamp DESC",
    )
    fun getAllEntriesForTheReviewQueue(): List<DataPointQaReviewEntity>

    /**
     * Find all QA information items filtering by company ID, data point identifier, reporting period and QA status provided via [filter].
     * Results are paginated using [resultLimit] and [resultOffset].
     * @param filter the filter to apply to the search containing the company ID, data point identifier, reporting period and the QA status
     * @param resultLimit the maximum number of results to return
     * @param resultOffset the offset to start the result set from
     */
    @Query(
        "SELECT dataPointQaReview FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE (:#{#filter.companyId} IS NULL OR dataPointQaReview.companyId = :#{#filter.companyId}) " +
            "AND (:#{#filter.dataPointType} IS NULL OR dataPointQaReview.dataPointType = :#{#filter.dataPointType}) " +
            "AND (:#{#filter.reportingPeriod} IS NULL OR dataPointQaReview.reportingPeriod = :#{#filter.reportingPeriod}) " +
            "AND (:#{#filter.qaStatus} IS NULL OR dataPointQaReview.qaStatus = :#{#filter.qaStatus})" +
            "ORDER BY dataPointQaReview.timestamp DESC " +
            "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun findByFilter(
        @Param("filter") filter: DataPointQaReviewItemFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<DataPointQaReviewEntity>

    /**
     * Find all QA information items filtering by company ID, data point identifier, reporting period and QA status provided via [filter].
     * Results are paginated using [resultLimit] and [resultOffset] and only contain the most recent entry per dataPointId.
     * @param filter the filter to apply to the search containing the company ID, data point identifier, reporting period and the QA status
     * @param resultLimit the maximum number of results to return
     * @param resultOffset the offset to start the result set from
     */
    @Query(
        "SELECT dataPointQaReview FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE dataPointQaReview.timestamp = " +
            "(SELECT MAX(subDataPointQaReview.timestamp) FROM DataPointQaReviewEntity subDataPointQaReview " +
            "WHERE subDataPointQaReview.dataPointId = dataPointQaReview.dataPointId) " +
            "AND (:#{#filter.companyId} IS NULL OR dataPointQaReview.companyId = :#{#filter.companyId}) " +
            "AND (:#{#filter.dataPointType} IS NULL OR dataPointQaReview.dataPointType = :#{#filter.dataPointType}) " +
            "AND (:#{#filter.reportingPeriod} IS NULL OR dataPointQaReview.reportingPeriod = :#{#filter.reportingPeriod}) " +
            "AND (:#{#filter.qaStatus} IS NULL OR dataPointQaReview.qaStatus = :#{#filter.qaStatus})" +
            "ORDER BY dataPointQaReview.timestamp DESC " +
            "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun findByFilterLatestOnly(
        @Param("filter") filter: DataPointQaReviewItemFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<DataPointQaReviewEntity>

    /**
     * Find the latest QA information items per dataPointId where the dataPointId is in the provided list [dataPointIds].
     */
    @Query(
        "SELECT dataPointQaReview FROM DataPointQaReviewEntity dataPointQaReview " +
            "WHERE dataPointQaReview.timestamp = " +
            "(SELECT MAX(subDataPointQaReview.timestamp) FROM DataPointQaReviewEntity subDataPointQaReview " +
            "WHERE subDataPointQaReview.dataPointId = dataPointQaReview.dataPointId) " +
            "AND  dataPointQaReview.dataPointId IN :#{#dataPointIds} " +
            "ORDER BY dataPointQaReview.timestamp DESC ",
    )
    fun findLatestWhereDataPointIdIn(dataPointIds: List<String>): List<DataPointQaReviewEntity>
}
