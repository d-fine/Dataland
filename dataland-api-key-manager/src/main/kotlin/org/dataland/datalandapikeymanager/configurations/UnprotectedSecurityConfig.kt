package org.dataland.datalandapikeymanager.configurations

import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy

/**
 * This class contains all security configurations for the api key manager to make all endpoints public.
 * It is only activated for the Spring profile "unprotected" to enable running unit tests without
 * the need of any token-authorization.
 */
@Profile("unprotected")
@KeycloakConfiguration
// TODO: get rid of deprecated WebSecurityConfigurerAdapter
class UnprotectedSecurityConfig : KeycloakWebSecurityConfigurerAdapter() {

    @Bean
    @Override
    override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return NullAuthenticatedSessionStrategy()
    }

    @Override
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests().anyRequest().permitAll()
        http.csrf().disable()
    }
}
