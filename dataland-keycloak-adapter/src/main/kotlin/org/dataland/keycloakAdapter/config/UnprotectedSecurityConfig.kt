package org.dataland.keycloakAdapter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.stereotype.Component

/**
 * This class contains all security configurations for the backend to make all endpoints public.
 * It is only activated for the Spring profile "unprotected" to enable running unit tests without
 * the need of any token-authorization.
 */
@Profile("unprotected")
@Component
class UnprotectedSecurityConfig {
    /**
     * Defines the Session Authentication Strategy
     */
    @Bean
    fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return NullAuthenticatedSessionStrategy()
    }

    /**
     * Defines the default Security Filter Chain
     */
    @Bean
    fun unprotectedFilterChain(http: HttpSecurity): DefaultSecurityFilterChain? {
        http
            .authorizeRequests().anyRequest().permitAll()
        http.csrf().disable()
        return http.build()
    }
}
