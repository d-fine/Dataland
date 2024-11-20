package org.dataland.datalandqaservice.repositories

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * A JPA repository for accessing QA information of a dataset
 */
interface QaReviewRepository : JpaRepository<QaReviewEntity, UUID> {
    /**
     * Find QA information for a specific dataId. Take first of all entries ordered by descending timestamp.
     */
    fun findFirstByDataIdOrderByTimestampDesc(dataId: String): QaReviewEntity?

    /**
     * Deletes QA information for a specific dataId.
     */
    fun deleteAllByDataId(dataId: String)

    /**
     * Find QA Information based on companyId, dataType, and reportingPeriod
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriod(
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
    ): List<QaReviewEntity>?

    /**
     * A function for getting a list of dataset IDs matching the filters in ascending order by timestamp
     * This query uses a CTE (Common Table Expression) in the following way: It creates a temporary result saved in the
     * table RankedByDataId which effectively partitions the entries by data_id (PARTITION BY) and orders each partition
     * by timestamp in a descending way; the ROW_NUMBER is then also assigned for each partition. In other words:
     * Entries with the same data_id are order by timestamp and the newest entry has num_row 1, the second newest
     * num_row 2 and so on.
     * Then, this CTE, i.e. only the newest entry for each dataId (the one with num_row = 1), is used in the filtered
     * query
     */
    @Query(
        nativeQuery = true,
        value =
            "WITH RankedByDataId AS (" +
                " SELECT *, ROW_NUMBER() OVER (PARTITION BY data_id ORDER BY timestamp DESC) AS num_row" +
                " FROM qa_review" +
                " )" +
                " SELECT entry.* FROM RankedByDataId entry" +
                " WHERE" +
                " entry.num_row = 1 AND" +
                " (:#{#searchFilter.shouldFilterByDataType} = false" +
                " OR entry.data_type IN :#{#searchFilter.preparedDataTypes}) AND" +
                " (:#{#searchFilter.shouldFilterByReportingPeriod} = false" +
                " OR entry.reporting_period IN :#{#searchFilter.preparedReportingPeriods}) AND" +
                " ( (:#{#searchFilter.shouldFilterByCompanyName } = false AND" +
                " :#{#searchFilter.shouldFilterByCompanyId} = false) AND" +
                " (:#{#searchFilter.shouldFilterByQaStatus} = false" +
                " OR entry.qa_status IN :#{#searchFilter.preparedQaStatuses})" +
                " OR entry.company_id IN :#{#searchFilter.preparedCompanyIds})" +
                " ORDER BY entry.timestamp ASC" +
                " LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun getSortedAndFilteredQaReviewMetadataSet(
        @Param("searchFilter") searchFilter: QaSearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<QaReviewEntity>

    /**
     * This query counts the number of datasets that matches the search filter and returns this number.
     */
    @Query(
        nativeQuery = true,
        value =
            "WITH RankedByDataId AS (" +
                " SELECT *, ROW_NUMBER() OVER (PARTITION BY data_id ORDER BY timestamp DESC) AS num_row" +
                " FROM qa_review" +
                " )" +
                " SELECT COUNT(*) FROM RankedByDataId entry" +
                " WHERE" +
                " entry.num_row = 1 AND" +
                " (:#{#searchFilter.shouldFilterByDataType} = false" +
                " OR entry.data_type IN :#{#searchFilter.preparedDataTypes}) AND" +
                " (:#{#searchFilter.shouldFilterByReportingPeriod} = false" +
                " OR entry.reporting_period IN :#{#searchFilter.preparedReportingPeriods}) AND" +
                " ( (:#{#searchFilter.shouldFilterByCompanyName } = false AND" +
                " :#{#searchFilter.shouldFilterByCompanyId} = false) AND" +
                " (:#{#searchFilter.shouldFilterByQaStatus} = false" +
                " OR entry.qa_status IN :#{#searchFilter.preparedQaStatuses})" +
                " OR entry.company_id IN :#{#searchFilter.preparedCompanyIds})",
    )
    fun getNumberOfFilteredQaReviews(
        @Param("searchFilter") searchFilter: QaSearchFilter,
    ): Int
}
