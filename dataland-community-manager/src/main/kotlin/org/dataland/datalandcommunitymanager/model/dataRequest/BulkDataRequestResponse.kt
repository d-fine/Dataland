package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty

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
    val acceptedDataRequests: List<DataRequestResponse>,
    @field:JsonProperty(required = true)
    val alreadyExistingNonFinalRequests: List<DataRequestResponse>,
    @field:JsonProperty(required = true)
    val alreadyExistingDatasets: List<DataSetsResponse>,
    @field:JsonProperty(required = true)
    val rejectedCompanyIdentifiers: List<String>,
)
