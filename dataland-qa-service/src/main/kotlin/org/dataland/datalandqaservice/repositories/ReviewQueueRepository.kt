package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewQueueEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * A JPA repository for accessing information about a reviewable dataset
 */
interface ReviewQueueRepository : JpaRepository<ReviewQueueEntity, String> {
    /**
     * A function for getting a list of dataset IDs with pending reviews ascendingly ordered by reception time
     */
    @Query(
        "SELECT status.dataId FROM ReviewQueueEntity status " +
            "ORDER BY status.receptionTime ASC",
    )
    fun getSortedPendingDataIds(): List<String>

    /**

     * Deletes queued QA request for a specific dataId.
     */
    fun deleteByDataId(dataId: String)
}
