package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the raw API Key and meta info associated to it
 * @param apiKey is the raw API key
 * @param apiKeyMetaInfo contains meta info about the API key (keycloakUserId, keycloakRoles and expiry Date)
 */
data class ApiKeyAndMetaInfo(
    @field:JsonProperty(required = true)
    val apiKey: String,

    // TDO do we have to set required to true here as well? I am not sure
    val apiKeyMetaInfo: ApiKeyMetaInfo,
)
