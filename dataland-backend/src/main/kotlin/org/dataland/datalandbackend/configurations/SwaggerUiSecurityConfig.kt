package org.dataland.datalandbackend.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

/**
 * This class is used to configure the CSP for the Swagger-UI as the default content does not allow loading anything
 */
@Configuration
@Order(SwaggerUiSecurityConfig.CONFIG_ORDER)
class SwaggerUiSecurityConfig {
    companion object {
        const val CONFIG_ORDER = 99
    }

    /**
     * The SecurityFilterChain Bean that defines the security for the swagger-ui
     */
    @Bean
    fun swaggerUiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(AntPathRequestMatcher.antMatcher("/swagger-ui/**"))
            // The provided hash is for the OAuth2 Redirect of the Swagger UI Login
            .headers().contentSecurityPolicy(
                "default-src 'self'; script-src 'self' 'sha256-4IiDsMH+GkJlxivIDNfi6qk0O5HPtzyvNwVT3Wt8TIw=';" +
                    " style-src 'self'; frame-ancestors 'self'; form-action 'self'; font-src 'self' data:;" +
                    " img-src 'self' data:"
            )
        return http.build()
    }
}
