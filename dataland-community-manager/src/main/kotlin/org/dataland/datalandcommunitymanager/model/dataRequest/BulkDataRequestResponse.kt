package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandcommunitymanager.utils.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a bulk data request to Dataland.
 * @param acceptedDataRequests contains information about all accepted data requests
 * @param alreadyExistingNonFinalRequests contains information about all already existing non-final data requests
 * @param alreadyExistingDatasets contains information about all already existing data sets
 * @param rejectedCompanyIdentifiers contains all company identifiers that were rejected by Dataland
 */
data class BulkDataRequestResponse(
    @field:JsonProperty(required = true)
    @Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ACCEPTED_DATA_REQUESTS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.ACCEPTED_DATA_REQUESTS_EXAMPLE,
    )
    val acceptedDataRequests: List<ResourceResponse>,
    @field:JsonProperty(required = true)
    @Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ALREADY_EXISTING_NON_FINAL_REQUESTS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.ALREADY_EXISTING_NON_FINAL_REQUESTS_EXAMPLE,
    )
    val alreadyExistingNonFinalRequests: List<ResourceResponse>,
    @field:JsonProperty(required = true)
    @Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ALREADY_EXISTING_DATASETS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.ALREADY_EXISTING_DATASETS_EXAMPLE,
    )
    val alreadyExistingDatasets: List<ResourceResponse>,
    @field:JsonProperty(required = true)
    @Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REJECTED_COMPANY_IDENTIFIERS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.REJECTED_COMPANY_IDENTIFIERS_EXAMPLE,
    )
    val rejectedCompanyIdentifiers: List<String>,
)
