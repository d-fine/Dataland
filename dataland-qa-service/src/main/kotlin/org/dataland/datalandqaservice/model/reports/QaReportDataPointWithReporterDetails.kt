package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointReviewDetails
import java.util.UUID

/**
 * A data point in a QA report with an ID.
 * @property qaReportId the ID of the QA report this data point belongs to
 * @property verdict the QA verdict for the data point
 * @property correctedData corrected data for the data point if applicable
 * @property reporterUserId the ID of the user who reported this data point
 * @property reporterCompanyId the ID of the company of the user who reported this data point
 */
@Entity
@Table(name = "dataset_review_qa_report_datapoint")
data class QaReportDataPointWithReporterDetails(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_point_review_details_id")
    val dataPointReviewDetails: DataPointReviewDetails?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMMENT_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMMENT_EXAMPLE,
    )
    @Id val qaReportId: UUID,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_DATA_POINT_VERDICT_DESCRIPTION,
    )
    val verdict: QaReportDataPointVerdict,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_CORRECTED_DATA_EXAMPLE,
    )
    val correctedData: String?,
    val reporterUserId: UUID,
    val reporterCompanyId: UUID,
)
