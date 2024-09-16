package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewQueueEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewInformationResponse
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
        "SELECT status.dataId, status.companyName, status.framework, status.reportingPeriod, status.receptionTime " +
            "FROM ReviewQueueEntity status " +
            "ORDER BY status.receptionTime ASC",
    )
    fun getSortedPendingMetadataSet(
        @Param("searchFilter") searchFilter: QaSearchFilter,
        @Param("resultLimit") resultLimit: Int? = 100,
        @Param("resultOffset") resultOffset: Int? = 0,
    ): List<ReviewInformationResponse>

    /**

     * Deletes queued QA request for a specific dataId.
     */
    fun deleteByDataId(dataId: String)
}
