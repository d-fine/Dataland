package org.dataland.datalandapikeymanager.model

/**
 * --- Non-API model ---
 * Class for defining the stored hashed API key, the meta info associated to it and the salt value for the respective
 * user
 * @param hashedApiKey is the hashed API key
 * @param apiKeyMetaInfo contains meta info about the API key (username, role and expiry date)
 * @param salt is a specific salt value just for this user/API key
 */
data class StoredHashedApiKey(
    val hashedApiKey: String,

    val apiKeyMetaInfo: ApiKeyMetaInfo,

    val salt: String
)
