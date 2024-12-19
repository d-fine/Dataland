package org.dataland.datalandbatchmanager.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi as BackendActuatorApi
import org.dataland.datalandcommunitymanager.openApiClient.api.ActuatorApi as CommunityActuatorApi

/**
 * A configuration class that provides access to pre-configured Api Clients
 */
@Configuration
class ApiClients(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
    @Value("\${dataland.communitymanager.base-url}") private val communityManagerBaseUrl: String,
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
    fun getBackendActuatorApi(): BackendActuatorApi = BackendActuatorApi(backendBaseUrl)

    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the community manager
     */
    @Bean
    fun getRequestControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): RequestControllerApi = RequestControllerApi(communityManagerBaseUrl, authenticatedOkHttpClient)

    /**
     * Creates an ActuatorApi of the community manager
     */
    @Bean
    fun getCommunityManagerActuatorApi(): CommunityActuatorApi = CommunityActuatorApi(communityManagerBaseUrl)
}
