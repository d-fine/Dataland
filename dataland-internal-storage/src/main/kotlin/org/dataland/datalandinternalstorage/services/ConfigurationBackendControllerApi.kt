package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationBackendControllerApi (
    @Value("\${dataland.backend.base-url:http://backend:8080/api}")
    private val backendBaseUrl: String,
) {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiBackendClient(): NonPersistedDataControllerApi {
        return NonPersistedDataControllerApi(basePath = backendBaseUrl)
    }
}
