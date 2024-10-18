package org.dataland.datalandcommunitymanager.model.dataRequest

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Contains all relevant info that a user should receive as response when posting a bulk data request to Dataland.
 * @param message contains a text about the result of Dataland processing the bulk data request
 * @param rejectedCompanyIdentifiers contains all company identifiers that were rejected by Dataland
 * @param acceptedCompanyIdentifiers contains all company identifiers that were accepted by Dataland
 */
data class BulkDataRequestResponse(
    @field:JsonProperty(required = true)
    val message: String,
    @field:JsonProperty(required = true)
    val acceptedCompanyIdentifiers: List<String>,
    @field:JsonProperty(required = true)
    val rejectedCompanyIdentifiers: List<String>,
)
