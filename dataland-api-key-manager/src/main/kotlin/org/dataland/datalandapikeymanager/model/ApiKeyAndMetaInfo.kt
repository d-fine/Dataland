package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the raw API Key and meta info associated to it
 * @param apiKey is the raw API key
 * @param apiKeyMetaInfo contains meta info about the API key (username, role and expiry date)
 */
data class ApiKeyAndMetaInfo(
    @field:JsonProperty(required = true)
    val apiKey: String,

    // TODO do we have to set required to true here as well? I am not sure
    val apiKeyMetaInfo: ApiKeyMetaInfo,
)
