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
 * @param invalidDataRequests contains information about all rejected requests due to validation errors
 * @param existingDataRequests contains information about all requests that were not created because there are already
 * existing requests for the same data dimension
 * @param existingDataSets contains information about all requests that were not created because there are already
 * datasets on dataland for that data dimension
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
                description = DataSourcingOpenApiDescriptionsAndExamples.INVALID_DATA_REQUESTS_DESCRIPTION,
            ),
    )
    val invalidDataRequests: List<BasicDataDimensions>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = BasicDataDimensions::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.EXISTING_DATA_REQUESTS_DESCRIPTION,
            ),
    )
    val existingDataRequests: List<BasicDataDimensions>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = BasicDataDimensions::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.EXISTING_DATA_SETS_DESCRIPTION,
            ),
    )
    val existingDataSets: List<BasicDataDimensions>,
)
