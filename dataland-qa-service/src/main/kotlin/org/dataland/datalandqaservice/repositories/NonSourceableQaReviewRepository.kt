package org.dataland.datalandqaservice.repositories

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Repository for non-sourceability QA review records.
 */
interface NonSourceableQaReviewRepository : JpaRepository<NonSourceableQaReviewInformationEntity, UUID> {
    /**
     * Finds all QA review records with the specified QA status, ordered by upload time ascending.
     *
     * @param qaStatus the QA status to filter by
     * @return a list of non-sourceability QA review entities
     */
    fun findAllByQaStatusOrderByUploadTimeAsc(qaStatus: QaStatus): List<NonSourceableQaReviewInformationEntity>

    /**
     * Finds all QA review records for a specific company, data type, and reporting period.
     *
     * @param companyId the company identifier
     * @param dataType the data type
     * @param reportingPeriod the reporting period
     * @return a list of non-sourceability QA review entities
     */
    fun findByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): List<NonSourceableQaReviewInformationEntity>
}
