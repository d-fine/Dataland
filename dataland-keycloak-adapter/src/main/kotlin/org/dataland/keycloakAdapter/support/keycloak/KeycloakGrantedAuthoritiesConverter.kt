package org.dataland.keycloakAdapter.support.keycloak

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

/**
 * Allows to extract granted authorities from a given JWT. The authorities
 * are determined by combining the realm (overarching) and client (application-specific)
 * roles, and normalizing them (configure them to the default format).
 */
@Component
class KeycloakGrantedAuthoritiesConverter() : Converter<Jwt, Collection<GrantedAuthority?>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmRoles = jwt.getClaimAsMap("realm_access")["roles"] as Collection<String>?
        return realmRoles?.map { SimpleGrantedAuthority(it) } ?: listOf()
    }
}
