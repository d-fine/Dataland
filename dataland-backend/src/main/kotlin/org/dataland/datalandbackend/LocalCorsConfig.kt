package org.dataland.datalandbackend

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Profile("development")

/**
 * Class is for local api route configuration
 */
class LocalCorsConfig {
    @Bean
    /**
     * Function for configure the MVC
     * @return cross origin configuration, here dedicated to allowance of client requests
     */
    fun corsConfigurer(): WebMvcConfigurer? {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:8090")
            }
        }
    }
}
