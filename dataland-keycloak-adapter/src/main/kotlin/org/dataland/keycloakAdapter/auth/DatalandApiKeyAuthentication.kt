package org.dataland.keycloakAdapter.auth

import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * This authentication class represents a successful authentication via a dataland API-Key
 */
class DatalandApiKeyAuthentication(
    private val apiKey: String,
    val apiKeyMetaInformation: ApiKeyMetaInfo
) : DatalandAuthentication() {
    override val userId: String
        get() = apiKeyMetaInformation.keycloakUserId!!

    override fun getAuthorities(): List<GrantedAuthority> {
        return apiKeyMetaInformation.keycloakRoles?.map { SimpleGrantedAuthority(it) } ?: emptyList()
    }

    override fun getCredentials(): String {
        return apiKey
    }
}
