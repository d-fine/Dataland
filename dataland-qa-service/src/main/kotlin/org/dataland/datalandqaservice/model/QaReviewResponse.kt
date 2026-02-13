package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity

/**
 * Comparable to the QaReviewEntity with the difference that the triggeringUserId is optional.
 * This class is used for the GET Response.
 */
data class QaReviewResponse(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val dataId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
    )
    val framework: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REVIEW_TIMESTAMP_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REVIEW_TIMESTAMP_EXAMPLE,
    )
    val timestamp: Long,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    var qaStatus: QaStatus,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMMENT_EXAMPLE,
    )
    val comment: String?,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REVIEWER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REVIEWER_ID_EXAMPLE,
    )
    val triggeringUserId: String?,
)

/**
 * Converts the QaReviewEntity into a QaReviewResponse which is used in a response for a GET Request.
 * The QaReviewResponse can optionally hide the triggeringUserId by setting showTriggeringUserId to false.
 */
fun QaReviewEntity.toQaReviewResponse(showTriggeringUserId: Boolean = false) =
    QaReviewResponse(
        dataId = this.dataId,
        companyId = this.companyId,
        companyName = this.companyName,
        framework = this.framework,
        reportingPeriod = this.reportingPeriod,
        timestamp = this.timestamp,
        qaStatus = this.qaStatus,
        comment = this.comment,
        triggeringUserId = if (showTriggeringUserId) this.triggeringUserId else null,
    )
