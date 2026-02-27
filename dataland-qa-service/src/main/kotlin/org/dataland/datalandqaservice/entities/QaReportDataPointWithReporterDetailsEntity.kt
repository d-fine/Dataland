package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportDataPointWithReporterDetails
import java.util.UUID

/**
 * JPA entity storing a single QA report data point with its reporter details, linked to a DataPointReviewDetailsEntity.
 */
@Suppress("LongParameterList")
@Entity
@Table(name = "dataset_review_qa_report_datapoint")
class QaReportDataPointWithReporterDetailsEntity(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_point_review_details_id")
    val dataPointReviewDetails: DataPointReviewDetailsEntity?,
    val qaReportId: UUID,
    val verdict: QaReportDataPointVerdict,
    val correctedData: String?,
    val reporterUserId: UUID,
    val reporterCompanyId: UUID,
) {
    /**
     * Converts this entity to its API response DTO.
     */
    fun toQaReportDataPointWithReporterDetails(): QaReportDataPointWithReporterDetails =
        QaReportDataPointWithReporterDetails(
            qaReportId = qaReportId,
            verdict = verdict,
            correctedData = correctedData,
            reporterUserId = reporterUserId,
            reporterCompanyId = reporterCompanyId,
        )
}
