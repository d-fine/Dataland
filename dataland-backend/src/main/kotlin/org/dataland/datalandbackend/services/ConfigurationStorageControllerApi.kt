package org.dataland.datalandbackend.services

import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationStorageControllerApi {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiInternalClient(): StorageControllerApi {
        return StorageControllerApi(
            basePath = if (isBackendInsideDockerContainer()) {
                "http://internal-storage:8080/internal-storage"
            } else {
                "https://local-dev.dataland.com/internal-storage"
            },
        )
    }

    /**
     * The function determines if it is run in a dockercontainer or not
     */
    fun isBackendInsideDockerContainer(): Boolean {
        val file = File("/.dockerenv")
        return file.exists()
    }
}
