package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

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
        description = BackendOpenApiDescriptionsAndExamples.REVIEWER_USER_NAME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REVIEWER_USER_NAME_EXAMPLE,
    )
    var reviewerUserName: String?,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.NUMBER_QA_REPORTS_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.NUMBER_QA_REPORTS_EXAMPLE,
    )
    var numberQaReports: Int,
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
