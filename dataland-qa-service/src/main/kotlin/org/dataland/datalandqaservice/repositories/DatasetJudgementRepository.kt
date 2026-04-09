package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    /**
     * Fetches dataset judgements together with their associated dataPoints in a single query to avoid
     * N+1 queries when converting entities to response objects that touch the collection.
     */
    @Query("select distinct d from DatasetJudgementEntity d left join fetch d.dataPoints where d.datasetId in :datasetIds")
    fun findAllByDatasetIdInWithDataPoints(@Param("datasetIds") datasetIds: Collection<UUID>): List<DatasetJudgementEntity>
}
