package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a bulk data request to Dataland.
 * @param acceptedDataRequests contains information about all accepted data requests
 * @param alreadyExistingRequests contains information about all already existing non-final data requests
 * @param alreadyExistingDatasets contains information about all already existing data sets
 * @param rejectedCompanyIdentifiers contains all company identifiers that were rejected by Dataland
 */
data class BulkDataRequestResponse(
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = ResourceResponse::class,
                description = CommunityManagerOpenApiDescriptionsAndExamples.ACCEPTED_DATA_REQUESTS_DESCRIPTION,
            ),
    )
    val acceptedDataRequests: List<ResourceResponse>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = ResourceResponse::class,
                description = CommunityManagerOpenApiDescriptionsAndExamples.ALREADY_EXISTING_REQUESTS_DESCRIPTION,
            ),
    )
    val alreadyExistingRequests: List<ResourceResponse>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = ResourceResponse::class,
                description = CommunityManagerOpenApiDescriptionsAndExamples.ALREADY_EXISTING_DATASETS_DESCRIPTION,
            ),
    )
    val alreadyExistingDatasets: List<ResourceResponse>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = CommunityManagerOpenApiDescriptionsAndExamples.REJECTED_COMPANY_IDENTIFIERS_DESCRIPTION,
                example = CommunityManagerOpenApiDescriptionsAndExamples.REJECTED_COMPANY_IDENTIFIERS_EXAMPLE,
            ),
    )
    val rejectedCompanyIdentifiers: List<String>,
)
