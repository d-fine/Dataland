package org.dataland.datalandbackendutils.apikey

/**
 * --- Non-API model ---
 * Class for defining the values which are derived from a received API key
 *
 * @param keycloakUserId is the part user id derived from the API-KEY
 * @param apiKeySecret is the secret derived from the API-KEY
 */
data class ParsedApiKey(
    val keycloakUserId: String,

    val apiKeySecret: String,
)
