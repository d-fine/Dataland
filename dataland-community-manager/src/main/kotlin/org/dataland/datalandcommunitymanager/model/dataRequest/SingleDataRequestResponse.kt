package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a single data request to Dataland.
 * @param message contains a text about the result of Dataland processing the single data request
 * @param reportingPeriodsOfStoredDataRequests reporting periods corresponding to the accepted and stored data requests
 * @param reportingPeriodsOfDuplicateDataRequests reporting periods corresponding to duplicate data requests on Dataland
 */
data class SingleDataRequestResponse(
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
    val reportingPeriodsOfStoredDataRequests: List<String>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_DESCRIPTION,
                example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_DUPLICATE_DATA_REQUESTS_EXAMPLE,
            ),
    )
    val reportingPeriodsOfDuplicateDataRequests: List<String>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_DESCRIPTION,
                example = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIODS_OF_STORED_ACCESS_REQUESTS_EXAMPLE,
            ),
    )
    val reportingPeriodsOfStoredAccessRequests: List<String>,
)
