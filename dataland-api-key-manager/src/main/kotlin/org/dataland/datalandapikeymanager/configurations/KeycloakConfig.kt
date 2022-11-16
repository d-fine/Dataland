package org.dataland.datalandapikeymanager.configurations

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Keycloak Config Resolver for Spring Boot
 */
@Configuration
class KeycloakConfig {
    @Bean

    /**
     * Method to use spring boot properties file instead of default keycloak.json
     * @return Keycloak Config Resolver for Spring Boot
     */
    fun keycloakConfigResolver(): KeycloakSpringBootConfigResolver? {
        return KeycloakSpringBootConfigResolver()
    }
}
