package org.dataland.datalandbackend.services

import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationStorageControlloerApi {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiInternalClient(): StorageControllerApi {
        return StorageControllerApi(
            basePath = "http://internal-storage:8080/internal-storage"
        )
    }
}
