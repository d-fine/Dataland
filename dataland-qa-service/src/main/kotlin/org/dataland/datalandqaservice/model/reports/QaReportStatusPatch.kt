package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * A patch for the status of a QA report.
 */
data class QaReportStatusPatch(
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.IS_REPORT_ACTIVE_DESCRIPTION,
    )
    val active: Boolean,
)
