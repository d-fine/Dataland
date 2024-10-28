package org.dataland.datalandemailservice.configurations

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
            .headers()
            .contentSecurityPolicy(
                "frame-src 'self' data: https://www.youtube.com https://www.youtube-nocookie.com/ " +
                    "https://consentcdn.cookiebot.com; script-src-elem 'self' 'unsafe-eval' " +
                    "'sha256-/0dJfWlZ9/P1qMKyXvELqM6+ycG3hol3gmKln32el8o=' https://consent.cookiebot.com " +
                    "https://consentcdn.cookiebot.com; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'; " +
                    "form-action 'self'; font-src 'self' data:; img-src 'self' data: " +
                    "https://*.googleusercontent.com/ https://*.licdn.com/ " +
                    "https://consent.cookiebot.com https://i.ytimg.com/",
            )
        return http.build()
    }
}
