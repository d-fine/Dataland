package org.dataland.datalandexternalstorage.services

import org.dataland.datalandeurodatclient.openApiClient.api.DatabaseCredentialResourceApi
import org.dataland.datalandeurodatclient.openApiClient.api.SafeDepositDatabaseResourceApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure this library in a spring-boot
 * environment
 */
@Configuration
class ConfigurationEurodatControllerApi(
    @Value("\${dataland.eurodatclient.base-url}")
    private val eurodatClientBaseUrl: String,
) {
    /**
     * The bean to configure the eurodat client CredentialControllerApi
     */
    @Bean
    fun getDatabaseCredentialResourceApiClient(): DatabaseCredentialResourceApi {
        return DatabaseCredentialResourceApi(basePath = eurodatClientBaseUrl)
    }

    @Bean
    fun SafeDepositDatabaseResourceApiClient(): SafeDepositDatabaseResourceApi {
        return SafeDepositDatabaseResourceApi(basePath = eurodatClientBaseUrl)
    }
}
