package org.dataland.documentmanager.configurations

import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationStorageControllerApi(
    @Value("\${dataland.internalstorage.base-url}")
    private val internalStorageBaseUrl: String,
) {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiInternalClient(): StorageControllerApi {
        return StorageControllerApi(basePath = internalStorageBaseUrl)
    }
}
