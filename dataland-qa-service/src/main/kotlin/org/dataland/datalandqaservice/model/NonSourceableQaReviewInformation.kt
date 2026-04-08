package org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples

/**
 * API model for QA-side non-sourceability review records.
 */
data class NonSourceableQaReviewInformation(
    @field:Schema(
        description = "Canonical non-sourceability record id used for cross-service correlation.",
        example = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE,
    )
    val nonSourceabilityId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    val qaStatus: QaStatus,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REASON_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REASON_EXAMPLE,
    )
    val reason: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.UPLOADER_USER_ID_EXAMPLE,
    )
    val uploaderUserId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.CREATION_TIME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.CREATION_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:Schema(
        description = "User id of the reviewer who recorded the QA decision.",
        example = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE,
        nullable = true,
    )
    val reviewerUserId: String? = null,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.COMMENT_EXAMPLE,
        nullable = true,
    )
    val qaComment: String? = null,
)
