package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.ReviewInformationEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the historical review information of a dataset
 */
interface ReviewHistoryRepository : JpaRepository<ReviewInformationEntity, String> {
    /**
     * Deletes QA information for a specific dataId.
     */
    fun deleteByDataId(dataId: String)
}
