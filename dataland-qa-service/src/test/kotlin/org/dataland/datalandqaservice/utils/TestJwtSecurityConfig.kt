package org.dataland.datalandqaservice.utils

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class TestJwtSecurityConfig {
    @Bean
    fun jwtDecoder(
        @Qualifier("oAuth2ResourceServerProperties") properties: OAuth2ResourceServerProperties,
    ): JwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri).build()
}
