package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for giving feedback via a response to a user who revokes her/his API key.
 * @param revokementProcessSuccessful defines if revoking the API key was successful
 * @param revokementProcessMessage contains a message to the user who requested to revoke her/his API key
 */
data class RevokeApiKeyResponse(
    @field:JsonProperty(required = true)
    val revokementProcessSuccessful: Boolean,

    @field:JsonProperty(required = true)
    val revokementProcessMessage: String
)
