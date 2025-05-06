package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

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
     * Retrieve all active data points for all triplets (company, type, period).
     * A data point is active when it is the latest accepted data point for the triplet
     * A data point is accepted when the latest entry per data id has the qaStatus accepted
     */
    @Query(
        "SELECT e " +
            "FROM DataPointQaReviewEntity e " +
            "WHERE e.qaStatus = org.dataland.datalandbackendutils.model.QaStatus.Accepted " +
            // ensure it is the latest within the data point
            "  AND e.timestamp = ( " +
            "    SELECT MAX(e1.timestamp)" +
            "    FROM DataPointQaReviewEntity e1" +
            "    WHERE e1.dataPointId = e.dataPointId" +
            "  )" +
            // ensure it is the latest within the group (company, type, period)
            "  AND e.timestamp = (" +
            "    SELECT MAX(e2.timestamp)" +
            "    FROM DataPointQaReviewEntity e2" +
            "    WHERE e2.companyId = e.companyId" +
            "      AND e2.dataPointType = e.dataPointType" +
            "      AND e2.reportingPeriod = e.reportingPeriod" +
            "      AND e2.qaStatus = org.dataland.datalandbackendutils.model.QaStatus.Accepted " +
            "      AND e2.timestamp = (" +
            "         SELECT MAX(e3.timestamp)" +
            "         FROM DataPointQaReviewEntity e3" +
            "         WHERE e3.dataPointId = e2.dataPointId)" +
            "  ) " +
            // Filter by company, type, period
            " AND e.companyId IN :companyIds " +
            " AND e.dataPointType IN :dataPointTypes " +
            " AND e.reportingPeriod IN :reportingPeriods ",
    )
    fun getActiveDataPointsForAllTriplets(
        @Param("companyIds") companyIds: List<String>,
        @Param("dataPointTypes") dataPointTypes: List<String>,
        @Param("reportingPeriods") reportingPeriods: List<String>,
    ): List<DataPointQaReviewEntity>

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
     * Find all QA entities by data point Ids
     */
    fun findAllByDataPointIdIn(dataPointIds: List<String>): List<DataPointQaReviewEntity>

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
