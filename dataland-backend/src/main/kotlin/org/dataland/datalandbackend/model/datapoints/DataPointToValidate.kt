package org.dataland.datalandbackend.model.datapoints

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Class defining the content of a body for data point validation
 * @param dataPoint the data point for validation as a JSON string
 * @param dataPointType which data point type the provided data is supposedly associated to
 */
data class DataPointToValidate(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_EXAMPLE,
    )
    val dataPoint: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_POINT_TYPE_EXAMPLE,
    )
    val dataPointType: String,
)
