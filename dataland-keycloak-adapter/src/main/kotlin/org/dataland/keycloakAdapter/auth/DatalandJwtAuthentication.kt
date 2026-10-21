package org.dataland.keycloakAdapter.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

/**
 * This authentication class represents a successful authentication via a Keycloak-Issued JWT token.
 */
class DatalandJwtAuthentication(
    private val jwt: Jwt,
) : DatalandAuthentication() {
    override val userId: String
        get() = checkNotNull(jwt.subject) { "JWT is missing the 'sub' claim." }

    val username: String
        get() = checkNotNull(jwt.getClaimAsString("preferred_username")) { "JWT is missing the 'preferred_username' claim." }

    val firstName: String
        get() = jwt.getClaimAsString("given_name") ?: ""

    val lastName: String
        get() = jwt.getClaimAsString("family_name") ?: ""

    override fun getAuthorities(): List<GrantedAuthority> =
        when (val realmRoles = jwt.getClaimAsMap("realm_access")?.get("roles")) {
            is Iterable<*> -> realmRoles.filterIsInstance<String>().map { SimpleGrantedAuthority(it) }
            else -> emptyList()
        }

    override fun getCredentials(): String = jwt.tokenValue

    val userDescription: String
        get() = "User $username (Keycloak ID: $userId)"
}
