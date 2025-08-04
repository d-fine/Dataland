package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * API model for the QA review information of a data point building on the DataPointDimensions interface.
 * @param dataPointId the id of the data point this review item is for
 * @param companyId the id of the company this data point belongs to
 * @param companyName the name of the company this data point belongs to
 * @param dataPointType the identifier of the type of data point
 * @param reportingPeriod the reporting period of the data point
 * @param timestamp the time the review was uploaded
 * @param qaStatus the QA status of the data point as provided by the reviewer
 * @param comment a comment explaining the verdict
 * @param reviewerId the id of the user who uploaded the review
 */
data class DataPointQaReviewInformation(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_ID_EXAMPLE,
    )
    val dataPointId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    override val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
    )
    override val dataPointType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REVIEW_TIMESTAMP_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REVIEW_TIMESTAMP_EXAMPLE,
    )
    val timestamp: Long,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    val qaStatus: QaStatus,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMMENT_EXAMPLE,
    )
    val comment: String?,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REVIEWER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REVIEWER_ID_EXAMPLE,
    )
    val reviewerId: String?,
) : DataPointDimensions
