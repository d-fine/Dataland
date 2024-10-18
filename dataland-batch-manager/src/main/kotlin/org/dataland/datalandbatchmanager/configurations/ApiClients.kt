package org.dataland.datalandbatchmanager.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
) {
    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the backend
     */
    @Bean
    fun getCompanyDataControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyDataControllerApi = CompanyDataControllerApi(backendBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an ActuatorApi of the backend
     */
    @Bean
    fun getBackendActuatorApi(): ActuatorApi = ActuatorApi(backendBaseUrl)
}
