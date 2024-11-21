package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a single data request to Dataland.
 * @param message contains a text about the result of Dataland processing the single data request
 * @param reportingPeriodsOfStoredDataRequests reporting periods corresponding to the accepted and stored data requests
 * @param reportingPeriodsOfDuplicateDataRequests reporting periods corresponding to duplicate data requests on Dataland
 */
data class SingleDataRequestResponse(
    @field:JsonProperty(required = true)
    val message: String,
    @field:JsonProperty(required = true)
    val reportingPeriodsOfStoredDataRequests: List<String>,
    @field:JsonProperty(required = true)
    val reportingPeriodsOfDuplicateDataRequests: List<String>,
    @field:JsonProperty(required = true)
    val reportingPeriodsOfStoredAccessRequests: List<String>,
)
