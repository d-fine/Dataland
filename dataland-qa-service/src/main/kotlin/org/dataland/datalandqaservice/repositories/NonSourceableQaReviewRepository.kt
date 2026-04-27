package org.dataland.datalandqaservice.repositories

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * JPA repository for [NonSourceableQaReviewInformationEntity].
 */
interface NonSourceableQaReviewRepository : JpaRepository<NonSourceableQaReviewInformationEntity, UUID> {
    /**
     * Finds the review entry for the given [nonSourceabilityId], if it exists.
     */
    fun findByNonSourceabilityId(nonSourceabilityId: String): NonSourceableQaReviewInformationEntity?

    /**
     * Returns all review entries filtered by optional qaStatus. Used by the listing endpoints.
     */
    @Query(
        """
        SELECT e FROM NonSourceableQaReviewInformationEntity e
        WHERE (:qaStatus IS NULL OR e.qaStatus = :qaStatus)
        ORDER BY e.uploadTime DESC
        """,
    )
    fun findByQaStatusFilter(
        @Param("qaStatus") qaStatus: QaStatus?,
    ): List<NonSourceableQaReviewInformationEntity>
}
