package org.dataland.keycloakAdapter.config

import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

/**
 * Configures JWT handling decoder
 */
@Configuration
internal class JwtSecurityConfig {
    /**
     * A Bean that provides a jwtDecoder - a thing that will convert strings to JWT-Objects
     */
    @Bean
    fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder {
        val jwkSetUri =
            checkNotNull(properties.jwt.jwkSetUri) {
                "Property 'spring.security.oauth2.resourceserver.jwt.jwk-set-uri' must be set."
            }
        val issuerUri =
            checkNotNull(properties.jwt.issuerUri) {
                "Property 'spring.security.oauth2.resourceserver.jwt.issuer-uri' must be set."
            }
        val jwtDecoder =
            NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .jwsAlgorithms { algs -> algs.addAll(setOf(SignatureAlgorithm.RS256, SignatureAlgorithm.ES256)) }
                .build()
        val validators = JwtValidators.createDefaultWithIssuer(issuerUri)
        jwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator(validators))
        return jwtDecoder
    }
}
