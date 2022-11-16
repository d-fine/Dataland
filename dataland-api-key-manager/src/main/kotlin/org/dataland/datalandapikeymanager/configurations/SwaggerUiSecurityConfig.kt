package org.dataland.datalandapikeymanager.configurations

import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * This class is used to configure the CSP for the Swagger-UI as the default content does not allow loading anything
 */
@Configuration
@Order(SwaggerUiSecurityConfig.CONFIG_ORDER)
class SwaggerUiSecurityConfig : WebSecurityConfigurerAdapter() {
    companion object {
        const val CONFIG_ORDER = 99
    }

    override fun configure(http: HttpSecurity) {
        http
            .antMatcher("/swagger-ui/**")
            // The provided hash is for the OAuth2 Redirect of the Swagger UI Login
            .headers().contentSecurityPolicy(
                "default-src 'self'; script-src 'self' 'sha256-4IiDsMH+GkJlxivIDNfi6qk0O5HPtzyvNwVT3Wt8TIw=';" +
                    " style-src 'self'; frame-ancestors 'self'; form-action 'self'; font-src 'self' data:;" +
                    " img-src 'self' data:"
            )
    }
}
