package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a bulk data request to Dataland.
 * @param acceptedDataRequests contains information about all accepted data requests
 * @param rejectedDataRequests contains information about all rejected requests due to valiation errors or because
 * the requests or corresponding datasets already exist
 */
data class BulkDataRequestResponse(
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = BasicDataDimensions::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.ACCEPTED_DATA_REQUESTS_DESCRIPTION,
            ),
    )
    val acceptedDataRequests: List<BasicDataDimensions>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = BasicDataDimensions::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.REJECTED_DATA_REQUESTS_DESCRIPTION,
            ),
    )
    val rejectedDataRequests: List<BasicDataDimensions>,
)
