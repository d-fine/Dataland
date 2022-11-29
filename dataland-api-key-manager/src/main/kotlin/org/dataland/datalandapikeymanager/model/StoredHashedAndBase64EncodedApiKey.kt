package org.dataland.datalandapikeymanager.model

/**
 * --- Non-API model ---
 * Class for defining the stored hashed API key, the meta info associated to it and the salt value for the respective
 * Keycloak user
 * @param apiKeyHashedAndBase64Encoded is the hashed API key as Base64 encoded string
 * @param apiKeyMetaInfo contains meta info about the API key (keycloakUserId, keycloakRoles and expiryDate)
 * @param saltBase64Encoded is a specific salt value just for this user/API key as Base64 encoded string
 */
data class StoredHashedAndBase64EncodedApiKey(
    val apiKeyHashedAndBase64Encoded: String,

    val apiKeyMetaInfo: ApiKeyMetaInfo,

    val saltBase64Encoded: String
)
