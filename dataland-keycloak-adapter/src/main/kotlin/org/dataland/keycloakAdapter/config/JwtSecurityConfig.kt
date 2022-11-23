package org.dataland.keycloakAdapter.config

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

/**
 * Configures JWT handling (decoder and validator)
 */
@Configuration
internal class JwtSecurityConfig {
    @Bean
    fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder {
        val jwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(properties.getJwt().getJwkSetUri())
            .jwsAlgorithms { algs -> algs.addAll(setOf(SignatureAlgorithm.RS256, SignatureAlgorithm.ES256)) }
            .build()
        val validators = JwtValidators.createDefaultWithIssuer(properties.jwt.issuerUri)
        jwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator(validators))
        return jwtDecoder
    }
}
