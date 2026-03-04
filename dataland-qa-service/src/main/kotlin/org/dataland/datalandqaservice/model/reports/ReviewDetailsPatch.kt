package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource

/**
 * Request body for patching the custom value of a data point in a dataset review.
 */
data class ReviewDetailsPatch(
    @field:Schema(
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE,
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_DESCRIPTION,
    )
    val customDataPoint: String? = null,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_DATA_POINT_SOURCE_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_DATA_POINT_SOURCE_EXAMPLE,
    )
    val acceptedSource: AcceptedDataPointSource? = null,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMPANY_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_QA_REPORT_COMPANY_ID_DESCRIPTION,
    )
    val companyIdOfAcceptedQaReport: String? = null,
)
