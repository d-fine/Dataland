package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * Request body for patching the custom value of a data point in a dataset review.
 */
data class CustomDataPointJSON(
    @field:Schema(
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE,
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_DESCRIPTION,
    )
    val customDataPoint: String? = null,
)
