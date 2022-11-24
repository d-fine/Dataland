<<<<<<<< HEAD:dataland-api-key-manager/src/main/kotlin/org/dataland/datalandapikeymanager/configurations/UnprotectedSecurityConfig.kt
package org.dataland.datalandapikeymanager.configurations
========
package org.dataland.keycloakAdapter.config
>>>>>>>> main:dataland-keycloak-adapter/src/main/kotlin/org/dataland/keycloakAdapter/config/UnprotectedSecurityConfig.kt

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.stereotype.Component

/**
 * This class contains all security configurations for the api key manager to make all endpoints public.
 * It is only activated for the Spring profile "unprotected" to enable running unit tests without
 * the need of any token-authorization.
 */
@Profile("unprotected")
<<<<<<<< HEAD:dataland-api-key-manager/src/main/kotlin/org/dataland/datalandapikeymanager/configurations/UnprotectedSecurityConfig.kt
@KeycloakConfiguration
// TODO: get rid of deprecated WebSecurityConfigurerAdapter
class UnprotectedSecurityConfig : KeycloakWebSecurityConfigurerAdapter() {

========
@Component
class UnprotectedSecurityConfig {
    /**
     * Defines the Session Authentication Strategy
     */
>>>>>>>> main:dataland-keycloak-adapter/src/main/kotlin/org/dataland/keycloakAdapter/config/UnprotectedSecurityConfig.kt
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
