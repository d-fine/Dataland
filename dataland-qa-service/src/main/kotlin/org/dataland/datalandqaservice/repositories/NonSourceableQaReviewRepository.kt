package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A JPA repository for accessing non-sourceable QA review information.
 */
@Repository
interface NonSourceableQaReviewRepository : JpaRepository<NonSourceableQaReviewInformationEntity, UUID> {
    /**
     * Find a review item by non-sourceability ID.
     */
    fun findByNonSourceabilityId(nonSourceabilityId: UUID): NonSourceableQaReviewInformationEntity?

    /**
     * Find all review items by QA status ordered by upload time descending.
     */
    fun findByQaStatusOrderByUploadTimeDesc(qaStatus: QaStatus): List<NonSourceableQaReviewInformationEntity>

    /**
     * Finds pending review items sorted for processing (oldest first).
     */
    @Query(
        "SELECT reviewItem FROM NonSourceableQaReviewInformationEntity reviewItem " +
            "WHERE reviewItem.qaStatus = org.dataland.datalandbackendutils.model.QaStatus.Pending " +
            "ORDER BY reviewItem.uploadTime ASC",
    )
    fun findPendingReviewQueue(): List<NonSourceableQaReviewInformationEntity>

    /**
     * Finds all review items for a given company and optional filters.
     */
    @Query(
        "SELECT reviewItem FROM NonSourceableQaReviewInformationEntity reviewItem " +
            "WHERE reviewItem.companyId = :companyId " +
            "AND (:dataType IS NULL OR reviewItem.dataType = :dataType) " +
            "AND (:reportingPeriod IS NULL OR reviewItem.reportingPeriod = :reportingPeriod) " +
            "AND (:qaStatus IS NULL OR reviewItem.qaStatus = :qaStatus) " +
            "ORDER BY reviewItem.uploadTime DESC",
    )
    fun findByFilter(
        @Param("companyId") companyId: String,
        @Param("dataType") dataType: String?,
        @Param("reportingPeriod") reportingPeriod: String?,
        @Param("qaStatus") qaStatus: QaStatus?,
    ): List<NonSourceableQaReviewInformationEntity>
}
