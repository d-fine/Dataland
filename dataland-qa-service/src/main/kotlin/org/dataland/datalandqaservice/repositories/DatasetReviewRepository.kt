package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A JPA repository for accessing dataset review information.
 */
@Repository
interface DatasetReviewRepository : JpaRepository<DatasetReviewEntity, UUID> {
    /**
     * Finds all DatasetReviewEntity objects by the given datasetId.
     *
     * @param datasetId The ID of the dataset.
     * @return A list of DatasetReviewEntity objects associated with the datasetId.
     */
    fun findAllByDatasetId(datasetId: UUID): List<DatasetReviewEntity>

    /**
     * Finds all DatasetReviewEntity objects by given datasetId and state.
     */
    fun findAllByDatasetIdAndReviewState(
        datasetId: UUID,
        reviewState: DatasetReviewState,
    ): List<DatasetReviewEntity>
}
