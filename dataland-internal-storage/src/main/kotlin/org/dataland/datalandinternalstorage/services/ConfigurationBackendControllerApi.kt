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
    //TODO Check if this is still true for deployement to a server
    @Bean
    fun getApiBackendClient(): NonPersistedDataControllerApi {
        return NonPersistedDataControllerApi(
                "http://host.docker.internal:8080/api"
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
