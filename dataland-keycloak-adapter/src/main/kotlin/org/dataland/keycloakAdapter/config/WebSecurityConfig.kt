package org.dataland.keycloakAdapter.config

import org.dataland.keycloakAdapter.support.apikey.ApiKeyAuthenticationManager
import org.dataland.keycloakAdapter.support.keycloak.KeycloakJwtAuthenticationConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter

/**
 * Configuration applied on all web endpoints defined for this
 * application. Any configuration on specific resources is applied
 * in addition to these global rules.
 */
@Configuration
@Profile("!unprotected")
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
    @Value("\${org.dataland.authorization.publiclinks:}") private val publicLinks: String,
    private val context: ApplicationContext
) {
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
    @Suppress("SpreadOperator")
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val publicLinksArray = publicLinks.split(",").toTypedArray()
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        if (context.containsBean("ApiKeyAuthenticationManager")) {
            val apiKeyAuthenticationManager = context.getBean("ApiKeyAuthenticationManager") as ApiKeyAuthenticationManager
            val apiKeyFilter = RequestHeaderAuthenticationFilter()
            apiKeyFilter.setPrincipalRequestHeader("dataland-api-key")
            apiKeyFilter.setExceptionIfHeaderMissing(false)
            apiKeyFilter.setAuthenticationManager(apiKeyAuthenticationManager)
            http.addFilterBefore(apiKeyFilter, AnonymousAuthenticationFilter::class.java)
        }

        http
            .authorizeRequests()
            .antMatchers(*publicLinksArray).permitAll()
            .anyRequest()
            .fullyAuthenticated().and()
            .csrf().disable()
            .oauth2ResourceServer().jwt().jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
        http
            .headers().contentSecurityPolicy("frame-ancestors 'none'; default-src 'none'")
            .and().referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)

        return http.build()
    }
}
