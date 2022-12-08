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
class ConfigurationStorageControlloerApi {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiInternalClient(): StorageControllerApi {
        val fileName ="/.dockerenv"
        var file = File(fileName)
        var fileExists =file.exists()
        return if (fileExists) {
            StorageControllerApi(
                basePath = "http://internal-storage:8080/internal-storage"
            )
        } else{
            StorageControllerApi(
                basePath = "http://localhost:8082/internal-storage"
            )
        }
    }
}
