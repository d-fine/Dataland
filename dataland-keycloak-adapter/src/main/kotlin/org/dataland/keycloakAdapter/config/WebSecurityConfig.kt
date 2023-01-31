package org.dataland.keycloakAdapter.config

import jakarta.servlet.http.HttpServletRequest
import org.dataland.keycloakAdapter.support.apikey.ApiKeyAuthenticationProvider
import org.dataland.keycloakAdapter.support.keycloak.KeycloakJwtAuthenticationProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

/**
 * Configuration applied on all web endpoints defined for this
 * application. Any configuration on specific resources is applied
 * in addition to these global rules.
 */
@Configuration
@Profile("!unprotected")
@EnableMethodSecurity
class WebSecurityConfig(
    private val jwtDecoder: JwtDecoder,
    @Value("\${dataland.authorization.publiclinks:}") private val publicLinks: String,
    @Value("\${dataland.apikeymanager.base-url}") private val apiKeyManagerBaseUrl: String,
    @Value("\${org.dataland.authorization.apikey.enable:false}") private val enableApiKeyAuthentication: Boolean,
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
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        authorizePublicLinksAndAddJwtConverter(http)
        updatePolicies(http)
        return http.build()
    }

    @Bean
    fun tokenAuthenticationManagerResolver(): AuthenticationManagerResolver<HttpServletRequest> {
        val jwtAuthenticationProvider = KeycloakJwtAuthenticationProvider(jwtDecoder)
        val authProviders = mutableListOf<AuthenticationProvider>(jwtAuthenticationProvider)

        if (enableApiKeyAuthentication) {
            val apiKeyAuthenticationProvider = ApiKeyAuthenticationProvider(apiKeyManagerBaseUrl)
            authProviders.add(0, apiKeyAuthenticationProvider)
        }

        val datalandAuthenticationManager = ProviderManager(authProviders)

        return AuthenticationManagerResolver { datalandAuthenticationManager }
    }

    @Suppress("SpreadOperator")
    private fun authorizePublicLinksAndAddJwtConverter(http: HttpSecurity) {
        val publicLinkMatchers = publicLinks.split(",").map { antMatcher(it) }.toTypedArray()
        http
            .authorizeHttpRequests()
            .requestMatchers(*publicLinkMatchers).permitAll()
            .anyRequest().fullyAuthenticated()
            .and()
            .logout().disable()
            .csrf().disable()
            .oauth2ResourceServer().authenticationManagerResolver(tokenAuthenticationManagerResolver())
    }

    private fun updatePolicies(http: HttpSecurity) {
        http
            .headers().contentSecurityPolicy("frame-ancestors 'none'; default-src 'none'")
            .and().referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
    }
}
