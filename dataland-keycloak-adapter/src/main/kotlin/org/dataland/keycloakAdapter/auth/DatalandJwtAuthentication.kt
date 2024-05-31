package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

/**
 * This authentication class represents a successful authentication via a Keycloak-Issued JWT token.
 */
class DatalandJwtAuthentication(private val jwt: Jwt) : DatalandAuthentication() {
    override val userId: String
        get() = jwt.subject

    val username: String
        get() = jwt.getClaimAsString("preferred_username")

    val firstName: String
        get() = jwt.getClaimAsString("firstName")

    val lastName: String
        get() = jwt.getClaimAsString("lastName")

    override fun getAuthorities(): List<GrantedAuthority> {
        val realmRoles = jwt.getClaimAsMap("realm_access")["roles"] as Collection<*>?
        return realmRoles?.map { SimpleGrantedAuthority(it as String) } ?: listOf()
    }

    override fun getCredentials(): String {
        return jwt.tokenValue
    }

    val userDescription: String
        get() = "User $username (Keycloak ID: $userId)"
}
