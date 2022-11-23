package org.dataland.datalandbackendutils.apikey

/**
 * --- Non-API model ---
 * Class for defining the values which are derived from a received API key to check their formats and validate the
 * API key.
 * @param parsedKeycloakUserIdBase64Encoded is the part of the API key which represents the Keycloak userId
 * @param parsedApiKeySecret is the part of the API key which is the actual secret of the API key
 * @param parsedCrc32Value is the part of the API key which is used for a cyclic redundancy check
 */
data class ParsedApiKey(
    val parsedKeycloakUserIdBase64Encoded: String,

    val parsedApiKeySecret: String,

    val parsedCrc32Value: String,

    val parsedApiKeyWithoutCrc32Value: String
)
