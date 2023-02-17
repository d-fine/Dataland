package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationBackendControllerApi {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiBackendClient(): NonPersistedDataControllerApi {
        val backendContainerUrl = "http://backend:8080/api"
        val internalBackendUrlFromEnv = System.getenv("INTERNAL_BACKEND_URL")
        return NonPersistedDataControllerApi(
            internalBackendUrlFromEnv?.ifBlank { backendContainerUrl } ?: backendContainerUrl,
        )
    }
}
