package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A JPA repository for accessing dataset review information.
 */
@Repository
interface DatasetJudgementRepository : JpaRepository<DatasetJudgementEntity, UUID> {
    /**
     * Finds all DatasetReviewEntity objects by the given datasetId.
     *
     * @param datasetId The ID of the dataset.
     * @return A list of DatasetReviewEntity objects associated with the datasetId.
     */
    fun findAllByDatasetId(datasetId: UUID): List<DatasetJudgementEntity>

    /**
     * Finds all DatasetJudgementEntity objects whose datasetId is in the given collection.
     * Used for bulk (batch) lookups to avoid N individual queries.
     *
     * @param datasetIds The collection of dataset IDs to look up.
     * @return A list of DatasetJudgementEntity objects associated with any of the given datasetIds.
     */
    fun findAllByDatasetIdIn(datasetIds: Collection<UUID>): List<DatasetJudgementEntity>
}
