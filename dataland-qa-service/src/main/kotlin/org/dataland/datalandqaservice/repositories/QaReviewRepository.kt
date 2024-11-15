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
     */
    @Query(
        nativeQuery = true,
        value =
            "SELECT entry.event_id, entry.data_id, entry.company_id, entry.company_name, entry.data_type, entry.reporting_period, " +
                "entry.timestamp, entry.qa_status, entry.reviewer_id, entry.comment " +
                "FROM qa_review entry " +
                "WHERE " +
                "(:#{#searchFilter.shouldFilterByDataType} = false " +
                "OR entry.data_type IN :#{#searchFilter.preparedDataTypes}) AND " +
                "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
                "OR entry.reporting_period IN :#{#searchFilter.preparedReportingPeriods}) AND " +
                "( (:#{#searchFilter.shouldFilterByCompanyName } = false AND " +
                ":#{#searchFilter.shouldFilterByCompanyId} = false) AND " +
                "(:#{#searchFilter.shouldFilterByQaStatus} = false " +
                "OR entry.qa_status IN :#{#searchFilter.preparedQaStatuses}) " +
                "OR entry.company_id IN :#{#searchFilter.preparedCompanyIds}) " +
                "ORDER BY entry.timestamp ASC " +
                "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun getSortedAndFilteredQaReviewMetadataSet(
        @Param("searchFilter") searchFilter: QaSearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<QaReviewEntity>

    /**
     * This query counts the number of datasets that matches the search fiter and returns this number.
     */
    @Query(
        nativeQuery = true,
        value =
            "SELECT COUNT(*) FROM qa_review entry " +
                "WHERE " +
                "(:#{#searchFilter.shouldFilterByDataType} = false " +
                "OR entry.data_type IN :#{#searchFilter.preparedDataTypes}) AND " +
                "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
                "OR entry.reporting_period IN :#{#searchFilter.preparedReportingPeriods}) AND " +
                "( (:#{#searchFilter.shouldFilterByCompanyName } = false AND " +
                ":#{#searchFilter.shouldFilterByCompanyId} = false) AND " +
                "(:#{#searchFilter.shouldFilterByQaStatus} = false " +
                "OR entry.qa_status IN :#{#searchFilter.preparedQaStatuses}) " +
                "OR entry.company_id IN :#{#searchFilter.preparedCompanyIds}) ",
    )
    fun getNumberOfFilteredQaReviews(
        @Param("searchFilter") searchFilter: QaSearchFilter,
    ): Int
}
