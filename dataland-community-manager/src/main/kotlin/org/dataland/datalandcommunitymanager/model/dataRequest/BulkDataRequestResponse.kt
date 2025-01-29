package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a bulk data request to Dataland.
 * @param message contains a text about the result of Dataland processing the bulk data request
 * @param acceptedDataRequests contains information about all accepted data requests
 * @param alreadyExistingDataRequests contains information about all already existing data requests
 * @param rejectedCompanyIdentifiers contains all company identifiers that were rejected by Dataland
 */
data class BulkDataRequestResponse(
    @field:JsonProperty(required = true)
    val message: String,
    @field:JsonProperty(required = true)
    val acceptedDataRequests: List<AcceptedDataRequestsResponse>,
    @field:JsonProperty(required = true)
    val alreadyExistingDataRequests: List<AlreadyExistingDataSetsResponse>,
    @field:JsonProperty(required = true)
    val rejectedCompanyIdentifiers: List<String>,
)
