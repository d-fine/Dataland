package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import java.util.UUID

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
                implementation = UUID::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_IDS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_IDS_EXAMPLE,
            ),
    )
    val idsOfStoredRequests: List<UUID>,
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
                implementation = UUID::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.DUPLICATE_REQUEST_IDS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.DUPLICATE_REQUEST_IDS_EXAMPLE,
            ),
    )
    val idsOfDuplicateRequests: List<UUID>,
)
