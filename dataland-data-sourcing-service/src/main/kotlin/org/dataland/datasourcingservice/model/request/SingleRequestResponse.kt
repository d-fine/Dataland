package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

data class SingleRequestResponse(
    @field:JsonProperty(required = true)
    @Schema(
        description = GeneralOpenApiDescriptionsAndExamples.POST_MESSAGE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.POST_MESSAGE_EXAMPLE,
    )
    val message: String,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_DESCRIPTION,
                example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_STORED_DATA_REQUESTS_EXAMPLE,
            ),
    )
    val reportingPeriodsOfStoredRequests: List<String>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_DESCRIPTION,
                example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_EXAMPLE,
            ),
    )
    val reportingPeriodsOfDuplicateRequests: List<String>,
)
