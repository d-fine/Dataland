package org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.entities.DatasetReviewStatusEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * A JPA repository for accessing the the review status of a dataset
 */
interface DatasetReviewStatusRepository : JpaRepository<DatasetReviewStatusEntity, String> {
    /**
     * A function for getting a list of dataset IDs with pending reviews ascendingly ordered by reception time
     */
    @Query(
        "SELECT status.dataId FROM DatasetReviewStatusEntity status " +
            "WHERE status.qaStatus = org.dataland.datalandbackendutils.model.QAStatus.Pending " +
            "ORDER BY status.receptionTime ASC",
    )
    fun getSortedPendingDataIds(): List<String>
}
