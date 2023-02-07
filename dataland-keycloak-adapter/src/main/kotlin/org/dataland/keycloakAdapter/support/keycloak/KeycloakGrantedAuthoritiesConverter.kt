package org.dataland.keycloakAdapter.support.keycloak

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

/**
 * Allows to extract granted authorities from a given JWT.
 */
@Component
class KeycloakGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority?>> {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * the convert method
     */
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmRoles = jwt.getClaimAsMap("realm_access")["roles"] as Collection<String>?
        logger.info("converted roles $realmRoles")
        return realmRoles?.map { SimpleGrantedAuthority(it) } ?: listOf()
    }
}
