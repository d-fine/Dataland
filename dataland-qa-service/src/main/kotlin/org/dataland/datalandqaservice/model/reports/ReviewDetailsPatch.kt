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
        description = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_DATA_POINT_SOURCE_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.ACCEPTED_DATA_POINT_SOURCE_EXAMPLE,
    )
    var acceptedSource: AcceptedDataPointSource? = null,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMPANY_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORT_COMPANY_ID_EXAMPLE,
    )
    var companyIdOfAcceptedQaReport: String? = null,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE,
    )
    var customDataPoint: String? = null,
)
