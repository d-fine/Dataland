package org.dataland.datalandbackend.configurations

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
     * Method to return a Keycloak Spring Boot Resolver object
     * @return Keycloak Config Resolver for Spring Boot
     */
    fun keycloakConfigResolver(): KeycloakSpringBootConfigResolver? {
        return KeycloakSpringBootConfigResolver()
    }
}
