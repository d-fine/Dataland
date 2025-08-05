package org.dataland.keycloakAdapter.config

import jakarta.servlet.http.HttpServletRequest
import org.dataland.keycloakAdapter.auth.provider.ApiKeyAuthenticationProvider
import org.dataland.keycloakAdapter.auth.provider.KeycloakJwtAuthenticationProvider
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.security.web.SecurityFilterChain
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
@EnableMethodSecurity
class WebSecurityConfig(
    private val jwtDecoder: JwtDecoder,
    @Value("\${dataland.authorization.publiclinks:}") private val publicLinks: String,
    @Value("\${dataland.authorization.internallinks:}") private val internalLinks: String,
    @Value("\${dataland.apikeymanager.base-url:}") private val apiKeyManagerBaseUrl: String,
    @Value("\${dataland.authorization.apikey.enable:false}") private val enableApiKeyAuthentication: Boolean,
) {
    /**
     * Defines the Session Authentication Strategy
     */
    @Bean
    fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy = NullAuthenticatedSessionStrategy()

    /**
     * Defines the default Security Filter Chain
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        authorizePublicLinksAndAddJwtConverter(http)
        updatePolicies(http)
        return http.build()
    }

    /**
     * Defines which AuthenticationProviders are to handle specific requests. JWT authentication is always enabled.
     * Api-Key authentication is enabled if enabled in the application settings.
     */
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
        val linksList = listStringToList(publicLinks) + listStringToList(internalLinks)

        http
            .authorizeHttpRequests {
                it
                    .requestMatchers(*linksList.toTypedArray())
                    .permitAll()
                    .anyRequest()
                    .fullyAuthenticated()
            }.logout {
                it.disable()
            }.csrf {
                it.disable()
            }.oauth2ResourceServer {
                it.authenticationManagerResolver(tokenAuthenticationManagerResolver())
            }
    }

    private fun listStringToList(listString: String) =
        if (listString.isNotEmpty()) {
            listString.split(",")
        } else {
            emptyList()
        }

    private fun updatePolicies(http: HttpSecurity) {
        http.headers { contentSecurityPolicy ->
            contentSecurityPolicy.contentSecurityPolicy {
                it.policyDirectives("frame-ancestors 'none'; default-src 'none'")
            }
            contentSecurityPolicy.referrerPolicy {
                it.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
            }
        }
    }
}
