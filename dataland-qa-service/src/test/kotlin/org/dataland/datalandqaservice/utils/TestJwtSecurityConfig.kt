package org.dataland.datalandqaservice.utils

import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class TestJwtSecurityConfig {
    @Bean
    fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder =
        NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri!!).build()
}
