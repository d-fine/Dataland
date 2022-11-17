package org.dataland.datalandbackend.configurations

import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.stereotype.Component

/**
 * This class contains all security configurations for the backend to secure endpoints with keycloak.
 */
@Profile("!unprotected")
@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
// TODO: get rid of deprecated WebSecurityConfigurerAdapter
class SecurityConfig : KeycloakWebSecurityConfigurerAdapter() {

    private val publicLinks = arrayOf(
        "/actuator/health",
        "/actuator/health/ping",
        "/actuator/info",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/companies/**",
        "/data/**",
        "/metadata",
        "/metadata/**"
    )

    /**
     * The Role Container is a component which maps actual roles from the Dataland keycloak service to the role
     * definitions which are used in the Dataland backend code to decouple the role namings in the keycloak service
     * from the ones in backend.
     */
    @Component("RoleContainer")
    object RoleContainer {
        const val DATA_READER = "USER"
        const val DATA_UPLOADER = "ADMIN"
    }

    /**
     * The SimpleAuthorityMapper is a simple one-to-one GrantedAuthoritiesMapper which allows for case conversion of
     * the authority name and the addition of a string prefix (which defaults to ROLE_ ).
     * @param auth is needed to build an AuthenticationProvider
     */
    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        val keycloakAuthenticationProvider = keycloakAuthenticationProvider()
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(SimpleAuthorityMapper())
        auth.authenticationProvider(keycloakAuthenticationProvider)
    }

    @Bean
    @Override
    override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return NullAuthenticatedSessionStrategy()
    }

    @Suppress("SpreadOperator")
    @Override
    override fun configure(http: HttpSecurity) {
        super.configure(http)
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            .antMatchers(*publicLinks).permitAll()
            .anyRequest().fullyAuthenticated()
            .and().csrf().disable()
        http
            .headers().contentSecurityPolicy("frame-ancestors 'none'; default-src 'none'")
            .and().referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
    }
}
