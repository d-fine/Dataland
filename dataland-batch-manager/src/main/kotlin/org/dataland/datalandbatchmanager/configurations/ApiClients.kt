package org.dataland.datalandbatchmanager.configurations

import okhttp3.OkHttpClient
import org.dataland.datalandbackend.openApiClient.api.ActuatorApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbatchmanager.service.KeycloakTokenManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * A configuration class that provides access to an auto-authenticated version of the CompanyDataControllerApi
 */
@Configuration
class ApiClients(
    @Autowired private val keycloakTokenManager: KeycloakTokenManager,
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
) {
    /**
     * Returns an OkHttpClient that automatically authenticates all requests
     */
    @Bean("AuthenticatedOkHttpClient")
    fun getAuthenticatedOkHttpClient(): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .addInterceptor {
                val originalRequest = it.request()
                val accessToken = keycloakTokenManager.getAccessToken()
                val modifiedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                it.proceed(modifiedRequest)
            }.build()
    }

    /**
     * Creates an auto-authenticated version of the CompanyDataControllerApi of the backend
     */
    @Bean
    fun getCompanyDataControllerApi(
        @Qualifier("AuthenticatedOkHttpClient") authenticatedOkHttpClient: OkHttpClient,
    ): CompanyDataControllerApi {
        return CompanyDataControllerApi(backendBaseUrl, authenticatedOkHttpClient)
    }

    /**
     * Creates an ActuatorApi of the backend
     */
    @Bean
    fun getBackendActuatorApi(): ActuatorApi {
        return ActuatorApi(backendBaseUrl)
    }
}
