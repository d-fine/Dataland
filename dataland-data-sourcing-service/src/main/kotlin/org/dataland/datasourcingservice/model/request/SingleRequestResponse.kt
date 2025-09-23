package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all information a user receives regarding a single data request he performed on Dataland.
 * @param message a message that informs the user about the result of his data request
 * @param reportingPeriodsOfStoredRequests a list of reporting periods for which data requests were stored
 * @param idsOfStoredRequests a list of ids of the stored data requests
 * @param reportingPeriodsOfDuplicateRequests a list of reporting periods for which duplicate data requests were found
 * @param idsOfDuplicateRequests a list of ids of the duplicate data requests
 */
data class SingleRequestResponse(
    @field:JsonProperty(required = true)
    @field:Schema(
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
                description = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_IDS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_IDS_EXAMPLE,
            ),
    )
    val idsOfStoredRequests: List<String>,
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
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DataSourcingOpenApiDescriptionsAndExamples.DUPLICATE_REQUEST_IDS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.DUPLICATE_REQUEST_IDS_EXAMPLE,
            ),
    )
    val idsOfDuplicateRequests: List<String>,
)
