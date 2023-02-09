package org.dataland.datalandbackend.configurations

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class is used to configure the CSP for the Swagger-UI as the default content does not allow loading anything
 */
@Configuration
class SwaggerUiConfig {
    @Bean
    /**
     * This method returns all public endpoints to be displayed in the swagger ui
     */
    fun adminApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder().group("public").pathsToMatch("/public/**").build()
    }
}
