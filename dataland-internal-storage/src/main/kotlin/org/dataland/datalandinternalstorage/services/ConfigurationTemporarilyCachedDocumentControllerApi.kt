package org.dataland.datalandinternalstorage.services

import org.dataland.documentmanager.openApiClient.api.TemporarilyCachedDocumentControllerApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationTemporarilyCachedDocumentControllerApi(
    @Value("\${dataland.document-manager.base-url}")
    private val documentManagerBaseUrl: String,
) {
    /**
     * The bean to configure the internal client TemporarilyCachedDocumentControllerApi
     */
    @Bean
    fun getTemporarilyCachedDocumentApiClient(): TemporarilyCachedDocumentControllerApi {
        return TemporarilyCachedDocumentControllerApi(basePath = documentManagerBaseUrl)
    }
}
