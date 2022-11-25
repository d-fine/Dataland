package org.dataland.keycloakAdapter.support.keycloak

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

/**
 * A Converter that converts JWTs to AuthenticationTokens
 */
@Component
class KeycloakJwtAuthenticationConverter(
    val grantedAuthoritiesConverter: KeycloakGrantedAuthoritiesConverter
) : Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * The convert method
     */
    override fun convert(jwt: Jwt): JwtAuthenticationToken {
        val authorities: Collection<GrantedAuthority> = grantedAuthoritiesConverter.convert(jwt)
        val username = jwt.subject
        return JwtAuthenticationToken(jwt, authorities, username)
    }
}
