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
     * The bean to configure the client for database credential retrieval from EuroDaT
     */
    @Bean
    fun getDatabaseCredentialResourceApiClient(): DatabaseCredentialResourceApi =
        DatabaseCredentialResourceApi(basePath = eurodatClientBaseUrl)

    /**
     * The bean to configure the client for safe deposit box creation in EuroDaT
     */
    @Bean
    fun getSafeDepositDatabaseResourceApiClient(): SafeDepositDatabaseResourceApi =
        SafeDepositDatabaseResourceApi(basePath = eurodatClientBaseUrl)
}
