package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

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
        return NonPersistedDataControllerApi(
            basePath = if (isInternalStorageInsideDockerContainer()) {
                "http://backend:8080/backend"
            } else {
                "http://localhost:8082/backend"
            }
        )
    }

    /**
     * The function determines if it is run in a dockercontainer or not
     */
    fun isInternalStorageInsideDockerContainer(): Boolean {
        val file = File("/.dockerenv")
        return file.exists()
    }
}
