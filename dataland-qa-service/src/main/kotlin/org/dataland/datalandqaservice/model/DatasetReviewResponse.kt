package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples

/**
 * API model for the dataset review objects returned.
 */
data class DatasetReviewResponse(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_ID_EXAMPLE,
    )
    val dataSetReviewId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val datasetId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        implementation = DataTypeEnum::class,
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_STATE_DESCRIPTION,
    )
    var reviewState: DatasetReviewState,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_REVIEWER_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_REVIEWER_EXAMPLE,
    )
    var reviewerUserId: String?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_REVIEWER_USERNAME_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_REVIEW_REVIEWER_USERNAME_EXAMPLE,
    )
    var reviewerUserName: String?,
    var dataPoints: Map<String, DataPointReviewOverview>,
)
