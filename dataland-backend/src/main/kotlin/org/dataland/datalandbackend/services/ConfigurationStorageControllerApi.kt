package org.dataland.datalandbackend.services

import org.dataland.datalandexternalstorage.openApiClient.api.ExternalStorageControllerApi
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
    @Value("\${dataland.externalstorage.base-url}")
    private val externalStorageBaseUrl: String,
) {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiInternalClient(): StorageControllerApi = StorageControllerApi(basePath = internalStorageBaseUrl)

    /**
     * The bean to configure the external client StorageControllerApi
     */
    @Bean
    fun getApiExternalClient(): ExternalStorageControllerApi = ExternalStorageControllerApi(basePath = externalStorageBaseUrl)
}
