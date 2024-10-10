package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewQueueEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewQueueResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing information about a reviewable dataset
 */
interface ReviewQueueRepository : JpaRepository<ReviewQueueEntity, String> {
    /**
     * A function for getting a list of dataset IDs with pending reviews ascendingly ordered by reception time
     */
    @Query(
        nativeQuery = true,
        value =
            "SELECT status.data_id, status.company_id, status.company_name, status.framework, status.reporting_period, " +
                "status.reception_time " +
                "FROM review_queue status " +
                "WHERE " +
                "(:#{#searchFilter.shouldFilterByDataType} = false " +
                "OR status.framework IN :#{#searchFilter.preparedDataTypes}) AND " +
                "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
                "OR status.reporting_period IN :#{#searchFilter.preparedReportingPeriods}) AND " +
                "( (:#{#searchFilter.shouldFilterByCompanyName } = false AND " +
                ":#{#searchFilter.shouldFilterByCompanyId} = false) " +
                "OR status.company_id IN :#{#searchFilter.preparedCompanyIds}) " +
                "ORDER BY status.reception_time ASC " +
                "LIMIT :#{#resultLimit} OFFSET :#{#resultOffset}",
    )
    fun getSortedPendingMetadataSet(
        @Param("searchFilter") searchFilter: QaSearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<ReviewQueueResponse>

    /**
     * This query counts the number of unreviewed datasets that matches the search fiter and returns this number.
     */
    @Query(
        nativeQuery = true,
        value =
            "SELECT COUNT(*) FROM review_queue status " +
                "WHERE " +
                "(:#{#searchFilter.shouldFilterByDataType} = false " +
                "OR status.framework IN :#{#searchFilter.preparedDataTypes}) AND " +
                "(:#{#searchFilter.shouldFilterByReportingPeriod} = false " +
                "OR status.reporting_period IN :#{#searchFilter.preparedReportingPeriods}) AND " +
                "( (:#{#searchFilter.shouldFilterByCompanyName } = false AND " +
                ":#{#searchFilter.shouldFilterByCompanyId} = false) " +
                "OR status.company_id IN :#{#searchFilter.preparedCompanyIds}) ",
    )
    fun getNumberOfRequests(
        @Param("searchFilter") searchFilter: QaSearchFilter,
    ): Int

    /**
     * Deletes queued QA request for a specific dataId.
     */
    fun deleteByDataId(dataId: String)
}
