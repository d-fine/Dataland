package org.dataland.datalandbackend.services

import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */

@Configuration
class ConfigurationDocumentsControllerApi(
    @Value("\${dataland.documentmanager.base-url}")
    private val documentManagerBaseUrl: String,
) {
    /**
     * The bean to configure the internal client StorageControllerApi
     */
    @Bean
    fun getApiDocumentsClient(): DocumentControllerApi {
        return DocumentControllerApi(basePath = documentManagerBaseUrl)
    }
}
